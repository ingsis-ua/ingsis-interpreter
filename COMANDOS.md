# Comandos de PrintScript

Todos se corren desde la raíz del repositorio. En Windows, `./gradlew` se reemplaza por
`.\gradlew.bat`.

## Preparar el entorno

| Comando | Qué hace |
|---|---|
| `export JAVA_HOME=$(/usr/libexec/java_home -v 25+)` | Usa un JDK 25 o más nuevo en esta terminal (macOS) |
| `./gradlew :cli:installDist` | Genera el ejecutable de la CLI en `cli/build/install/printscript` |
| `alias printscript="JAVA_HOME=$(/usr/libexec/java_home -v 25+) $PWD/cli/build/install/printscript/bin/printscript"` | Crea el comando `printscript` mientras dure la terminal |
| `./gradlew installGitHooks` | Instala el hook de pre-commit (una vez por clon) |
| `git config core.hooksPath` | Muestra si el hook está instalado: responde `config/githooks` |

## La CLI: las cuatro operaciones

| Comando | Qué hace |
|---|---|
| `printscript execution programa.ps` | Ejecuta el programa e imprime su salida |
| `printscript exec programa.ps` | Lo mismo, con el alias corto |
| `printscript execution programa.ps -o salida.txt` | Ejecuta, muestra la salida y además la guarda en `salida.txt` |
| `printscript validation programa.ps` | Valida la sintaxis y la semántica sin ejecutar (alias `validate`) |
| `printscript formatting programa.ps` | Formatea sin reglas: el código sale como estaba (alias `format`, `fmt`) |
| `printscript formatting -c formato.json programa.ps` | Formatea con las reglas del archivo y muestra el resultado |
| `printscript formatting -c formato.json programa.ps -o formateado.ps` | Formatea y guarda el resultado en otro archivo; nunca pisa el fuente |
| `printscript analyzing programa.ps` | Analiza con el linter; sin configuración solo revisa la sintaxis (alias `analyze`, `lint`) |
| `printscript analyzing -c reglas.yaml programa.ps` | Analiza con las reglas del archivo (`.json`, `.yaml` o `.yml`) |

## La CLI: opciones y ayuda

| Comando | Qué hace |
|---|---|
| `printscript --help` | Muestra la ayuda general (también `-h`) |
| `printscript execution --help` | Muestra la ayuda de una operación (vale para las cuatro) |
| `printscript execution -v 1.1 programa.ps` | Usa la versión 1.1 del lenguaje (también `--version`; vale para las cuatro; la predeterminada es 1.0) |
| `printscript execution programa.ps 2>/dev/null` | Muestra solo la salida del programa, sin el progreso ni los errores |
| `echo $?` | Muestra el código de salida: `0` éxito, `1` errores en el `.ps`, `2` error de uso |
| `printscript` | Sin argumentos, abre el modo interactivo |
| `./gradlew :cli:run --console=plain` | Abre el modo interactivo sin instalar la CLI |
| `./gradlew :cli:run --args="execution programa.ps"` | Corre una operación sin instalar la CLI |

## Modo interactivo (REPL)

Las rutas son relativas al directorio que muestra `pwd`.

| Comando | Alias | Qué hace |
|---|---|---|
| `help` | `?`, `ayuda` | Muestra todos los comandos |
| `pwd` | | Muestra el directorio actual |
| `ls [directorio]` | `dir` | Lista el contenido del directorio |
| `cd <directorio>` | | Cambia de directorio (acepta `..` y rutas absolutas) |
| `run <archivo>` | `exec`, `execution` | Ejecuta el archivo |
| `save <archivo>` | `guardar` | Ejecuta y guarda la salida en `<archivo>.out.txt` |
| `validate <archivo>` | `validation` | Valida sin ejecutar |
| `format <archivo> [config]` | `fmt`, `formatting` | Formatea, con reglas opcionales |
| `analyze <archivo> [config]` | `lint`, `analyzing` | Analiza con el linter, con reglas opcionales |
| `version` | `v` | Muestra la versión del lenguaje de la sesión |
| `version 1.1` | `v 1.1` | Cambia la versión para las operaciones siguientes |
| `exit` | `quit`, `salir` | Sale del modo interactivo (también con Ctrl-D) |

## Gradle: build

| Comando | Qué hace |
|---|---|
| `./gradlew build` | Compila, arma los jars y corre todo `check` |
| `./gradlew assemble` | Compila y arma los jars, sin tests ni chequeos |
| `./gradlew clean` | Borra las carpetas `build/` para empezar de cero |
| `./gradlew clean build jacocoRootReport` | Hace el build completo desde cero, igual que el CI |
| `./gradlew :lexer:build` | Hace el build de un solo módulo |
| `./gradlew build --stacktrace` | Repite el build mostrando el detalle técnico del error |
| `./gradlew build --warning-mode all` | Muestra todas las deprecaciones |
| `./gradlew tasks` | Lista las tareas disponibles por categoría |
| `./gradlew tasks --all` | Lista también las tareas de cada módulo |
| `./gradlew help --task cpdCheck` | Explica una tarea concreta |
| `./gradlew projects` | Muestra los 13 módulos del proyecto |
| `./gradlew :cli:dependencies --configuration runtimeClasspath` | Muestra el árbol de dependencias de un módulo |
| `./gradlew --version` | Muestra las versiones de Gradle, Kotlin y el JVM |
| `./gradlew --stop` | Detiene los daemons de Gradle |
| `./gradlew :runner:playground` | Ejecuta un ejemplo fijo para desarrollar el runner |

## Tests

| Comando | Qué hace |
|---|---|
| `./gradlew test` | Corre todos los tests y genera sus reportes de cobertura |
| `./gradlew :parser:test` | Corre los tests de un módulo |
| `./gradlew :parser:test --tests 'org.printscript.parser.IfParserTest'` | Corre una sola clase de test |
| `./gradlew :runner:test --tests '*ConsignaExamplesEndToEndTest'` | Busca la clase de test por nombre, sin el paquete |
| `./gradlew test --rerun` | Vuelve a correr los tests aunque Gradle los considere al día |

## Calidad: formatter, linter y cobertura

| Comando | Qué hace |
|---|---|
| `./gradlew check` | Corre tests, formato, Checkstyle, PMD, CPD y los mínimos de cobertura |
| `./gradlew spotlessCheck` | Verifica el formato sin cambiar nada |
| `./gradlew spotlessApply` | Corrige el formato del Java y de los archivos de build/texto |
| `./gradlew :parser:spotlessApply` | Corrige el formato de un solo módulo |
| `./gradlew spotlessCheck --rerun-tasks` | Vuelve a verificar el formato sin reutilizar resultados anteriores |
| `./gradlew checkstyleMain` | Corre Checkstyle sobre el código de producción |
| `./gradlew checkstyleTest` | Corre Checkstyle sobre los tests |
| `./gradlew pmdMain` | Corre PMD sobre el código de producción |
| `./gradlew pmdTest` | Corre PMD sobre los tests |
| `./gradlew cpdCheck` | Busca código duplicado |
| `./gradlew jacocoTestCoverageVerification` | Falla si la cobertura de un módulo queda bajo su mínimo |
| `./gradlew jacocoTestReport` | Genera el reporte de cobertura de cada módulo |
| `./gradlew jacocoRootReport` | Genera el reporte de cobertura conjunto del proyecto |
| `./scripts/test-pre-commit.sh` | Prueba en un clon temporal que el hook de pre-commit funcione |

## La CLI: distribución

| Comando | Qué hace |
|---|---|
| `./gradlew :cli:distZip` | Arma la CLI en un `.zip` listo para descomprimir y usar |
| `./gradlew :cli:distTar` | Lo mismo, en formato `.tar` |

## TCK y publicación

| Comando | Qué hace |
|---|---|
| `./tck/run-official-tck.sh 0.2.0-SNAPSHOT` | Publica en Maven Local y corre los casos del TCK de la cátedra |
| `./tck/run-official-tck.sh <versión> --published` | Corre el TCK contra una versión ya publicada en GitHub Packages |
| `./gradlew publishToMavenLocal` | Publica los módulos en Maven Local, sin tocar el remoto |
| `./gradlew publish -PprintscriptVersion=<versión>` | Publica los 13 módulos en GitHub Packages (necesita credenciales) |
| `GITHUB_ACTOR=<usuario> GITHUB_TOKEN=<token> ./scripts/publish-working-tree.sh <versión>` | Corre build, cobertura y TCK, y si pasan publica el working tree |

## CI y mantenimiento

| Comando | Qué hace |
|---|---|
| `gh run list --branch master` | Muestra las últimas corridas del CI |
| `gh run view <id> --log-failed` | Muestra solo los pasos que fallaron en una corrida |
| `./gradlew wrapper --gradle-version <versión>` | Actualiza el wrapper a otra versión de Gradle |

open build/reports/jacoco/jacocoRootReport/html/index.html
