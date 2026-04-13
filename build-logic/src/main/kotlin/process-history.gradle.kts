plugins {
    kotlin("jvm") version "1.9.23"
    application
}

application {
    mainClass.set("ProcessHistoryKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
}

tasks.register<JavaExec>("processHistory") {
    group = "verification"
    description = "Process history JSONL files into a single log file"
    mainClass.set("ProcessHistoryKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = project.rootDir
}
