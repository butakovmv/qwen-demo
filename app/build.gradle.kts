plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    id("com.gorylenko.gradle-git-properties")
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1")
    }
}

dependencies {
    implementation(project(":operation"))
    implementation(project(":postgres"))
    implementation(project(":web-api"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
}

gitProperties {
    failOnNoGitDirectory = false
}

// ── Thin JAR + отдельные JAR-зависимости ──────────────────────────────

val copyDependencies by tasks.registering(Copy::class) {
    group = "build"
    description = "Copy runtime dependencies to build/libs/lib/"

    val runtimeClasspath = configurations.runtimeClasspath.get()
    from(runtimeClasspath)
    into(layout.buildDirectory.dir("libs/lib"))
}

tasks.jar {
    enabled = true
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to "miotaxi.aidemo.OtusApplicationKt",
            "Class-Path" to
                configurations
                    .runtimeClasspath
                    .get()
                    .map { jar -> "lib/${jar.name}" }
                    .joinToString(" "),
        )
    }
    finalizedBy(copyDependencies)
}

// Задача для удобного запуска из lib/
tasks.register<JavaExec>("runThin") {
    group = "application"
    description = "Run the application with thin JAR and lib/ folder"
    dependsOn("jar", copyDependencies)

    val jarFile = tasks.jar.get().archiveFile.get().asFile
    val libDir = layout.buildDirectory.dir("libs/lib").get().asFile

    classpath = files(jarFile) + fileTree(libDir)
    mainClass.set("miotaxi.aidemo.OtusApplicationKt")
}
