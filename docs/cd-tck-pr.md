# CD: sincronizar el fork del TCK después de validar la publicación

El job `actualizar-tck` depende del éxito de `publicar` y `validar-publicacion`.
Usa los adapters del SHA publicado, configura Java 25 y el heap del TCK validado,
y fija las seis dependencias directas y todas las transitivas de `org.printscript`
a esa misma versión. El fork resuelve ese grupo exclusivamente desde GitHub Packages,
sin Maven Local. No modifica los tests. Hace un commit y un push normal a la rama
configurada; si no hay cambios, no crea un commit. Un fallo de sincronización marca
el CD como fallido aunque los paquetes ya estén publicados; no los borra ni revierte.
El Release de tags sigue dependiendo de la validación.

## Configuración por única vez

El destino actual es `Vontk/printscript-tck`, rama `master`, y la App privada
`PrintScript TCK CD Vontk` tiene ID `5501019`. Está instalada únicamente en ese fork.
En Actions del intérprete se configuraron `TCK_FORK_REPOSITORY`, `TCK_FORK_BRANCH`,
`TCK_APP_ID` y el secret `SECRET`, que debe contener el PEM completo de la clave de la App.

Para reproducir la configuración:

1. Identificar el fork de `austral-ingsis/printscript-tck`. Se recomienda alojarlo en
   la organización del equipo, con permiso **Write** para los compañeros y Admin/Owner
   reservado a administradores. Un repositorio personal tiene un único dueño; agregar
   colaboradores no los convierte en copropietarios. No hace falta dar Owner a todos.
2. Crear una GitHub App sin webhooks con **Contents: Read and write** y Metadata
   de lectura, sin permisos de cuenta, organización, paquetes ni workflows. Instalarla
   usando **Only select repositories**, seleccionando únicamente el fork.
3. En `ingsis-ua/ingsis-interpreter`, Settings → Secrets and variables → Actions,
   configurar `TCK_FORK_REPOSITORY=Vontk/printscript-tck`, `TCK_FORK_BRANCH=master`
   y `TCK_APP_ID=5501019` como variables. Generar una clave privada de la App y guardar
   su PEM completo como secret `SECRET`. No incluir la clave en el PR, archivos ni
   mensajes. El job obtiene un token temporal limitado al fork y lo revoca al terminar.
   Para rotar la clave, reemplazar el secret y revocar la anterior cuando funcione.
4. La rama de destino debe permitir pushes de la App. Si exige PR o firmas, elegir
   una rama de integración que los permita o configurar una excepción específica
   conforme a las reglas del equipo. No desactivar protecciones globalmente.
5. Los paquetes deben permitir publicación/lectura por los workflows del intérprete,
   como hasta ahora. La App del fork no autentica GitHub Packages.

**Only on this account** limita dónde se instala la App: alcanza mientras el fork
esté en Vontk. No limita quién puede disparar el CD del repositorio compartido.
Los cambios de cualquier compañero que lleguen a main/master y pasen CI disparan
el mismo CD, con la misma configuración de Actions. Nadie necesita instalar la App
en su cuenta ni recibir su clave. Si se traslada el fork a otra cuenta, habrá que
trasladar también la App o habilitar su instalación en esa cuenta y actualizar las variables.

El `GITHUB_TOKEN` automático está limitado al repositorio del workflow: dar más
permisos a los compañeros no lo habilita para escribir en otro repositorio. La App
evita usar un PAT personal compartido. La App actual pertenece a Vontk; para que
también su administración quede en manos del equipo se recomienda trasladarla, junto
al fork, a la organización. Eso no es necesario para que los compañeros usen el CD.

## En la máquina de cada compañero

El CD corre en GitHub Actions. No hay que instalar un agente ni la clave de la App
localmente. Para inspeccionar o correr el fork actualizado:

```sh
git clone git@github.com:Vontk/printscript-tck.git
cd printscript-tck
git switch master
git pull --ff-only
```

Para SSH, cada persona registra su propia clave SSH en GitHub. HTTPS también sirve;
el fork público puede clonarse sin permisos de escritura. Para contribuir al fork
se necesita una invitación de colaborador; para contribuir al intérprete, acceso
al repositorio/equipo y respetar sus reglas de merge. Instalar Git y JDK 25; usar el
wrapper del fork si soporta Java 25 o Gradle 9.5.0, usado para validar el TCK:

```sh
gradle --version
gradle clean test --no-daemon
```

Para **descargar Maven Packages localmente**, GitHub exige autenticación incluso
para paquetes públicos. Cada compañero crea su propio PAT **classic** con
`read:packages`, teniendo lectura del repositorio de los paquetes y SSO autorizado
si la organización lo exige. No requiere `write:packages` para correr el TCK.
Configurar fuera del repositorio, en `~/.gradle/gradle.properties`:

```properties
gpr.user=SU_USUARIO
gpr.key=SU_PROPIO_PAT_CLASSIC
```

Proteger el archivo (`chmod 600 ~/.gradle/gradle.properties` en Linux/macOS), usar
vencimiento y revocar el PAT cuando deje de necesitarse. Alternativamente, exportar
`GITHUB_ACTOR` y `GITHUB_TOKEN` en la propia sesión. Ser dueño o colaborador del fork
no elimina la autenticación del registro Maven. **No compartir tokens ni la clave
de la App**. Para trabajar sin PAT local, ejecutar el TCK de este repositorio contra
Maven Local (`./tck/run-official-tck.sh VERSION`); eso comprueba fuentes locales,
no descarga los paquetes publicados.

## Operación y límites

Cada publicación validada, de rama principal o tag, actualiza la rama configurada.
La versión refleja la última sincronización completada, no necesariamente el mayor
SemVer: evitar publicaciones simultáneas si se necesita ese orden. Los pushes no usan
force; un rechazo requiere revisar el avance del fork y reintentar. Un reintento
completo publica otra versión main; para conservar la versión ya publicada,
reejecutar solamente el job fallido. No activar publicación duplicada en el fork.

## Validación

Se verificaron sintaxis Bash, estructura del workflow y sincronización con un remoto
Git temporal, incluida repetición sin cambios y rechazo de versiones inválidas.
La primera corrida real después del merge debe comprobar que el commit del fork
contiene los adapters, `printscript.gradle` y `printscript-version.txt` con la versión
publicada por ese CD. No se publicó una versión real para probar este cambio.

Referencias oficiales:

- [Acceso a otros repositorios](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/about-authentication-to-github)
- [Actions con una GitHub App](https://docs.github.com/en/apps/creating-github-apps/authenticating-with-a-github-app/making-authenticated-api-requests-with-a-github-app-in-a-github-actions-workflow)
- [Instalar una GitHub App](https://docs.github.com/en/apps/using-github-apps/installing-your-own-github-app)
- [Autenticación del registro Gradle](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)
