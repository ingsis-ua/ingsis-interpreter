#!/usr/bin/env bash
# Prepara y publica un checkout del fork; la autenticación la provee checkout/SSH.
set -euo pipefail
if [[ $# -ne 3 || ! "$2" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[[:alnum:].]+)?$ || ! "$3" =~ ^[0-9a-f]{40}$ ]]; then
    echo 'Uso: sync-fork.sh <checkout-fork> <version> <sha-interpreter>' >&2
    exit 2
fi
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FORK_DIR="$(cd "$1" && pwd)"
VERSION="$2"
SOURCE_COMMIT="$3"
[[ -z "$(git -C "$FORK_DIR" status --porcelain)" ]] || { echo 'El checkout del fork debe estar limpio' >&2; exit 1; }
[[ -f "$FORK_DIR/build.gradle" && -f "$FORK_DIR/settings.gradle" && -d "$FORK_DIR/src/main/java/implementation" ]]
cp "$ROOT_DIR"/tck/implementation/*.java "$FORK_DIR/src/main/java/implementation/"
rm -f "$FORK_DIR/src/main/java/implementation/NotImplementedException.java"
cp "$ROOT_DIR/tck/published.gradle" "$FORK_DIR/printscript.gradle"
printf '%s\n' "$VERSION" > "$FORK_DIR/printscript-version.txt"
# El bloque solo se agrega una vez; el resto del build y los tests se conservan.
if ! grep -Fxq "apply from: 'printscript.gradle'" "$FORK_DIR/build.gradle"; then
    printf "\napply from: 'printscript.gradle'\n" >> "$FORK_DIR/build.gradle"
fi
# El CI del fork corre con JDK 21: el resolver de foojay descarga el toolchain 25.
if ! grep -Fq 'org.gradle.toolchains.foojay-resolver-convention' "$FORK_DIR/settings.gradle"; then
    { printf "plugins {\n    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.9.0'\n}\n"
      cat "$FORK_DIR/settings.gradle"; } > "$FORK_DIR/settings.gradle.tmp"
    mv "$FORK_DIR/settings.gradle.tmp" "$FORK_DIR/settings.gradle"
fi
git -C "$FORK_DIR" add -- build.gradle settings.gradle printscript.gradle printscript-version.txt src/main/java/implementation
if git -C "$FORK_DIR" diff --cached --quiet; then
    echo 'El fork ya está actualizado'
    exit 0
fi
git -C "$FORK_DIR" -c user.name='printscript-cd[bot]' -c user.email='printscript-cd[bot]@users.noreply.github.com' \
    commit -m "chore: PrintScript $VERSION" -m "Source: ingsis-ua/ingsis-interpreter@$SOURCE_COMMIT"
# Nunca forzar: una actualización concurrente se informa como fallo y se reintenta.
git -C "$FORK_DIR" push origin HEAD
