#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "Uso: ./scripts/publish-working-tree.sh <version-unica>" >&2
    exit 2
fi

readonly GRADLE_PROPERTIES="${GRADLE_USER_HOME:-$HOME/.gradle}/gradle.properties"
has_property() {
    [[ -f "$GRADLE_PROPERTIES" ]] && grep -q "^$1=" "$GRADLE_PROPERTIES"
}
if [[ -z "${GITHUB_ACTOR:-}" || -z "${GITHUB_TOKEN:-}" ]] \
    && ! { has_property gpr.user && has_property gpr.key; }; then
    echo "Faltan GITHUB_ACTOR/GITHUB_TOKEN (o gpr.user/gpr.key en $GRADLE_PROPERTIES)." >&2
    exit 2
fi

readonly VERSION="$1"

# Publica exactamente los fuentes presentes, incluso cambios todavía no commiteados,
# pero solo después de que pasen calidad, tests internos y el TCK oficial.
./gradlew clean build jacocoRootReport -PprintscriptVersion="$VERSION" \
    --no-daemon --warning-mode all
./tck/run-official-tck.sh "$VERSION"
./gradlew publish -PprintscriptVersion="$VERSION" --no-daemon
