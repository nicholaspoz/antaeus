plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-core"))
    implementation(project(":pleo-antaeus-models"))

    implementation("io.javalin:javalin:2.6.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
}
