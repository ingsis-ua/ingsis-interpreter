plugins {
    id("maven-publish")
}

// group y version los fija el convention plugin printscript.java-library
// (org.printscript / 0.1.0). El artifactId sale del nombre del módulo.
//
// Para el TDD local publicamos con `publishToMavenLocal` y el TCK resuelve
// desde `mavenLocal()`. El repositorio de GitHub Packages queda declarado para
// la fase de publicación real: apunta a ingsis-ua/ingsis-interpreter y toma las
// credenciales de las propiedades gpr.user/gpr.key o de las variables de entorno
// GITHUB_ACTOR/GITHUB_TOKEN. Mientras no se invoque la tarea de publicación a ese
// repo, las credenciales nulas no molestan al flujo local.
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ingsis-ua/ingsis-interpreter")
            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
