#!/usr/bin/env bash
set -euo pipefail

readonly ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

readonly CLONE="$WORK_DIR/clon"
git clone --quiet "$ROOT_DIR" "$CLONE"
cp "$ROOT_DIR/config/githooks/pre-commit" "$CLONE/config/githooks/pre-commit"
cd "$CLONE"
git config user.email "hook@test"
git config user.name "hook"
git config core.hooksPath config/githooks
git add config/githooks/pre-commit
git commit --quiet --no-verify --allow-empty -m "hook bajo prueba"

readonly FILE="token/src/main/java/org/printscript/token/Token.java"
readonly GOOD="$(cat "$FILE")"
readonly BAD="${GOOD/public record Token(/public record Token  (}"

failures=0

check() {
    local description="$1" expected="$2"
    local actual
    if git commit --quiet -m "prueba" >/dev/null 2>&1; then
        actual="aceptado"
        git reset --quiet --soft HEAD~1
    else
        actual="rechazado"
    fi
    if [[ "$actual" == "$expected" ]]; then
        echo "ok    $description: $actual"
    else
        echo "FALLA $description: se esperaba $expected y fue $actual"
        failures=$((failures + 1))
    fi
}

restore() {
    git reset --quiet --hard HEAD
    git clean --quiet -fd
}

printf '%s\n' "$BAD" > "$FILE"
git add "$FILE"
printf '%s\n' "$GOOD" > "$FILE"
check "staged mal formateado, disco bien" rechazado
[[ "$(git show :"$FILE")" == "$BAD" ]] || { echo "FALLA el index cambió"; failures=$((failures + 1)); }
[[ "$(cat "$FILE")" == "$GOOD" ]] || { echo "FALLA el working tree cambió"; failures=$((failures + 1)); }
restore

printf '%s\n' "$BAD" > "$FILE"
echo "notas" > NOTAS.md
git add NOTAS.md
check "mal formateado sin stagear, commit de otro archivo" aceptado
[[ "$(cat "$FILE")" == "$BAD" ]] || { echo "FALLA el archivo sin stagear se perdió"; failures=$((failures + 1)); }
restore

printf '%s\n' "$BAD" > "$FILE"
git add "$FILE"
check "staged mal formateado" rechazado
restore

echo "x" > SUELTO.txt
printf '%s\n' "$GOOD" > "$FILE"
echo "notas" > NOTAS.md
git add NOTAS.md
check "commit limpio con archivos sin seguimiento" aceptado
[[ -f SUELTO.txt ]] || { echo "FALLA el archivo sin seguimiento se perdió"; failures=$((failures + 1)); }
restore

if [[ $failures -gt 0 ]]; then
    echo "$failures verificaciones fallaron"
    exit 1
fi
echo "todas las verificaciones pasaron"
