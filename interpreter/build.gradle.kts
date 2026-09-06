plugins {
    id("printscript.java-library")
    id("printscript.quality")
    id("printscript.format")
    id("printscript.publish")
}

dependencies {
    api(project(":common"))
    api(project(":ast"))
    api(project(":runtime-api"))
}
