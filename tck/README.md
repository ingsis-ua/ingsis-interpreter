# TCK de la cátedra y validación en el fork

El [TCK de la cátedra](https://github.com/austral-ingsis/printscript-tck) define
`PrintScriptFactory` y 59 pruebas de aceptación. Los adapters de `implementation/`
conectan esas interfaces con las fachadas de `runner`; no contienen lógica del lenguaje.

## Corrección verificable del test de memoria

El test original `InterpreterLargeFileTest.testWithCollector` exige que coleccionar
32.768 mensajes agote un heap de 7 MB. En Java 21, el colector termina correctamente:
58/59 pruebas pasan y la restante falla por esa expectativa dependiente de la JVM.

`patches/InterpreterLargeFileTest.java` mantiene los dos escenarios originales
y corrige esa expectativa: si el colector termina, debe contener los 32.768 mensajes
correctos; si se agota la memoria, debe reportar `Java heap space`. Agrega además
una prueba que limita a 1.024 líneas el adelanto de lectura sobre la salida emitida.
Una implementación que lee todo el programa antes de emitir falla en esta prueba;
lo comprobamos con una variante intencionalmente eager. El heap conserva los límites
de 5 y 7 MB definidos por la cátedra. El TCK adaptado pasa **60/60** localmente.

El archivo corregido se copia explícitamente en el checkout aislado del TCK. El
repositorio upstream y los otros tests no se modifican. La diferencia queda
versionada aquí para que se pueda revisar y proponer a la cátedra.

## CI local

```sh
./tck/run-official-tck.sh 0.2.0-SNAPSHOT
```

El script publica los módulos del working tree en Maven Local, clona el commit oficial
`510028a8246f6fd607a3195394310f509d9047a2`, copia los adapters y la corrección
del test, y ejecuta el TCK. `tck.init.gradle` solo declara las dependencias.

Para usar un checkout local del TCK sin red:

```sh
PRINTSCRIPT_TCK_SOURCE=../printscript-tck ./tck/run-official-tck.sh 0.2.0-SNAPSHOT
```

## CD en el fork

Cada push a `master` (o `main`) que pasa el CI activa el CD; esto incluye los merges
de pull requests. El CD publica una versión única de los módulos en GitHub Packages.
Después, `run-fork-ci.sh` clona el mismo
commit oficial, crea una rama única en `Vontk/printscript-tck`, agrega adapters,
corrección del test, `fork-build.gradle` y `fork-ci.yml`, y la envía con
`TCK_FORK_TOKEN`. Ese push dispara el CI del fork, que descarga la versión publicada
y ejecuta los 60 casos. El CD espera la corrida asociada al SHA enviado y falla si
algún caso falla. El fork no usa Maven Local.

Configurar en el repositorio principal `TCK_FORK_TOKEN`, con permisos para escribir
contenido y workflows y leer Actions en el fork. Configurar
en el fork `TCK_PACKAGES_USER` y `TCK_PACKAGES_TOKEN`, con lectura de los paquetes
del repositorio principal. Para GitHub Packages Maven, `TCK_PACKAGES_TOKEN` debe ser
un personal access token (classic) con `read:packages` y acceso al paquete; el
usuario corresponde a ese token. El CI del fork falla explícitamente si faltan.
