#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 || ! "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[[:alnum:].]+)?$ ]]; then
    echo "Uso: ./tck/run-fork-ci.sh <versión publicada>" >&2
    exit 2
fi

: "${TCK_FORK_TOKEN:?Falta TCK_FORK_TOKEN para escribir en el fork y consultar Actions}"
: "${TCK_FORK_REPOSITORY:?Falta TCK_FORK_REPOSITORY (owner/repo)}"
: "${GITHUB_RUN_ID:?Falta GITHUB_RUN_ID para crear una rama única}"

if [[ ! "$TCK_FORK_REPOSITORY" =~ ^[[:alnum:]_.-]+/[[:alnum:]_.-]+$ ]]; then
    echo "TCK_FORK_REPOSITORY debe ser owner/repo" >&2
    exit 2
fi

readonly VERSION="$1"
readonly ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly TCK_COMMIT="510028a8246f6fd607a3195394310f509d9047a2"
readonly TCK_SOURCE="${PRINTSCRIPT_TCK_SOURCE:-https://github.com/austral-ingsis/printscript-tck.git}"
readonly BRANCH="release/v${VERSION}-run-${GITHUB_RUN_ID}-${GITHUB_RUN_ATTEMPT:-1}"
readonly WORK_DIR="$(mktemp -d)"

cleanup() {
    rm -rf "$WORK_DIR"
}
trap cleanup EXIT

git clone --quiet "$TCK_SOURCE" "$WORK_DIR/tck"
git -C "$WORK_DIR/tck" checkout --quiet "$TCK_COMMIT"
git -C "$WORK_DIR/tck" switch --quiet -c "$BRANCH"

cp "$ROOT_DIR/tck/implementation/CustomImplementationFactory.java" \
    "$WORK_DIR/tck/src/main/java/implementation/CustomImplementationFactory.java"
cp "$ROOT_DIR/tck/implementation/PrintScriptAdapters.java" \
    "$WORK_DIR/tck/src/main/java/implementation/PrintScriptAdapters.java"
cp "$ROOT_DIR/tck/patches/InterpreterLargeFileTest.java" \
    "$WORK_DIR/tck/src/test/java/interpreter/InterpreterLargeFileTest.java"
rm "$WORK_DIR/tck/src/main/java/implementation/NotImplementedException.java"
cp "$ROOT_DIR/tck/fork-build.gradle" "$WORK_DIR/tck/build.gradle"
cp "$ROOT_DIR/tck/fork-ci.yml" "$WORK_DIR/tck/.github/workflows/gradle.yml"

git -C "$WORK_DIR/tck" add build.gradle .github/workflows/gradle.yml \
    src/main/java/implementation src/test/java/interpreter/InterpreterLargeFileTest.java
git -C "$WORK_DIR/tck" -c user.name='PrintScript CD' \
    -c user.email='printscript-cd@users.noreply.github.com' \
    commit --quiet -m "Validate PrintScript ${VERSION} against official TCK"
readonly TCK_SHA="$(git -C "$WORK_DIR/tck" rev-parse HEAD)"

if [[ "${TCK_FORK_PREPARE_ONLY:-}" == 1 ]]; then
    echo "Fork preparado en $TCK_SHA"
    exit 0
fi

# Un token del fork (PAT o GitHub App) es necesario: los pushes hechos con el
# GITHUB_TOKEN del repositorio principal no disparan otro workflow de push.
cat > "$WORK_DIR/askpass" <<'ASKPASS'
#!/usr/bin/env bash
case "$1" in
    *Username*) printf '%s\n' 'x-access-token' ;;
    *) printf '%s\n' "$TCK_FORK_TOKEN" ;;
esac
ASKPASS
chmod 700 "$WORK_DIR/askpass"
GIT_ASKPASS="$WORK_DIR/askpass" GIT_TERMINAL_PROMPT=0 \
    git -C "$WORK_DIR/tck" push --quiet \
    "https://github.com/${TCK_FORK_REPOSITORY}.git" "HEAD:refs/heads/$BRANCH"

export GH_TOKEN="$TCK_FORK_TOKEN"
run_id=''
for attempt in {1..40}; do
    run_id="$(gh run list --repo "$TCK_FORK_REPOSITORY" --commit "$TCK_SHA" \
        --workflow gradle.yml --event push --json databaseId --jq '.[0].databaseId // empty')"
    if [[ -n "$run_id" ]]; then
        break
    fi
    sleep 15
done

if [[ -z "$run_id" ]]; then
    echo "No apareció el CI del fork para $TCK_SHA" >&2
    exit 1
fi

echo "TCK del fork: https://github.com/${TCK_FORK_REPOSITORY}/actions/runs/${run_id}"
timeout 30m gh run watch "$run_id" --repo "$TCK_FORK_REPOSITORY" --exit-status
