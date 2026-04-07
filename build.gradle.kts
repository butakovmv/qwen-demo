import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.spring") version "1.9.23" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("com.gorylenko.gradle-git-properties") version "2.5.7" apply false
}

allprojects {
    group = "miotaxi.aidemo"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://gitverse.ru/api/packages/maven")
        maven("https://maven.sbercloud.ru/repository/maven-public")
    }
}

tasks.register("lint") {
    group = "verification"
    description = "Run all linters (detekt + ktlint + frontend ESLint/Prettier)"
    subprojects.filter { it.name != "front" }.forEach { proj ->
        dependsOn(proj.tasks.matching { it.name in setOf("detekt", "ktlintCheck") })
    }
    dependsOn(":front:npmLint")
    dependsOn(":front:npmFormatCheck")
}

tasks.register("fix") {
    group = "formatting"
    description = "Auto-fix all linting issues (ktlint + frontend ESLint/Prettier)"
    subprojects.filter { it.name != "front" }.forEach { proj ->
        dependsOn(proj.tasks.matching { it.name == "ktlintFormat" })
    }
    dependsOn(":front:npmFormatFix")
}

subprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    if (name != "front") {
        apply(plugin = "io.gitlab.arturbosch.detekt")
        apply(plugin = "org.jlleitschuh.gradle.ktlint")

        // Spring Dependency Management может переопределять Kotlin-версии в detekt.
        afterEvaluate {
            configurations.matching { it.name == "detekt" }.all {
                resolutionStrategy.eachDependency {
                    if (requested.group == "org.jetbrains.kotlin") {
                        useVersion("1.9.23")
                    }
                }
            }
        }

        configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        }

        configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            verbose.set(true)
            android.set(false)
            outputToConsole.set(true)
            ignoreFailures.set(false)
            filter {
                exclude("**/generated/**")
            }
        }
    }
}

// Включаем frontend-тесты в test для backend-модулей
subprojects
    .filter { it.name in listOf("app", "usecase", "web-api") }
    .forEach { proj ->
        proj.afterEvaluate {
            proj.tasks.findByName("test")?.dependsOn(":front:test")
        }
    }
