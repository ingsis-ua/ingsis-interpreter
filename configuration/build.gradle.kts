plugins {
    id("printscript.java-library")
    id("printscript.quality")
    id("printscript.format")
    id("printscript.publish")
}

dependencies {
    api(project(":common"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.18.2"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
}
