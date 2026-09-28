# TCK oficial y validación en el fork

El [TCK de la cátedra](https://github.com/austral-ingsis/printscript-tck) define
`PrintScriptFactory` y 59 pruebas de aceptación. Los adapters de `implementation/`
conectan esas interfaces con las fachadas de `runner`; no contienen lógica del lenguaje.

## CI local

```sh
./tck/run-official-tck.sh 0.2.0-SNAPSHOT
```

El script publica los módulos del working tree en Maven Local, clona el commit oficial
`510028a8246f6fd607a3195394310f509d9047a2`, copia los adapters y ejecuta el TCK.
`tck.init.gradle` solo declara las dependencias del adapter. El TCK conserva su
toolchain Java 21 y sus límites de heap de 5 y 7 MB.

Para usar un checkout local del TCK sin red:

```sh
PRINTSCRIPT_TCK_SOURCE=../printscript-tck ./tck/run-official-tck.sh 0.2.0-SNAPSHOT
```

## CD en el fork

Después de publicar una versión en GitHub Packages, `run-fork-ci.sh` clona el mismo
commit oficial, crea una rama única en `Vontk/printscript-tck`, agrega los adapters,
`fork-build.gradle` y `fork-ci.yml`, y la envía con `TCK_FORK_TOKEN`. Ese push dispara
el CI del fork, que descarga la versión publicada y ejecuta los tests originales.
El CD espera el resultado de la corrida asociada al SHA enviado y falla si el TCK
falla. No usa Maven Local ni modifica los tests del TCK en el fork.

Configurar en el repositorio principal el secreto `TCK_FORK_TOKEN`, con permisos
`Contents: write`, `Workflows: write`, `Actions: read` y `Secrets: read` sobre el fork. Configurar
en el fork `TCK_PACKAGES_USER` y `TCK_PACKAGES_TOKEN`, con lectura de los paquetes
del repositorio principal. El CD verifica que existan antes de publicar.

## Estado observado

En Java 21, con los límites de memoria originales del TCK, pasan 58 de 59 casos.
`InterpreterLargeFileTest.testWithCollector` espera que guardar 32.768 salidas
agote 7 MB de heap y reporte `Java heap space`; en la ejecución local el colector
terminó sin agotar memoria. Ese resultado depende de la JVM y de cómo representa
las cadenas. La automatización informa el fallo y detiene la entrega sin alterar
el límite de heap ni el test.
