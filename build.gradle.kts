import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.spring") version "1.9.23" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("com.gorylenko.gradle-git-properties") version "2.5.7" apply false
    jacoco
    java
    base
}

// Корневая задача test: запускает тесты всех модулей + формирует отчёты
tasks.named<Test>("test") {
    group = "verification"
    description = "Run all backend and frontend tests with aggregated reports"
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    finalizedBy("testReport", "coverageReport")
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

// ── Агрегированные отчёты ──────────────────────────────────────────────

// Единый отчёт по покрытию — все модули (JaCoCo XML + LCOV)
tasks.register<GenerateCoverageReportTask>("coverageReport") {
    group = "verification"
    description = "Generate aggregated coverage report for all modules (JaCoCo + Vitest)"

    val jacocoModules = subprojects.filter { it.plugins.hasPlugin("jacoco") }
    jacocoModules.forEach { proj ->
        val testTasks = proj.tasks.withType<Test>()
        dependsOn(testTasks)
        testTasks.forEach { it.ignoreFailures = true }
    }
    dependsOn(jacocoModules.map { it.tasks.named("jacocoTestReport") })
    dependsOn(":front:test")

    // JaCoCo XML из backend-модулей с jacoco
    val jacocoXml = jacocoModules.map { proj ->
        proj.layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml")
    }
    jacocoXmlFiles.from(jacocoXml)

    // LCOV из frontend
    val frontLcov = project(":front").projectDir.resolve("coverage/lcov.info")
    lcovFiles.from(layout.file(provider { frontLcov }))

    outputHtml.set(layout.buildDirectory.file("reports/coverage.html"))
}

// Единый отчет по тестам
tasks.register<GenerateTestReportTask>("testReport") {
    group = "verification"
    description = "Generate self-contained HTML test report"

    subprojects.forEach { proj ->
        val testTasks = proj.tasks.withType<Test>()
        dependsOn(testTasks)
        testTasks.forEach { it.ignoreFailures = true }
    }

    backendTestResultsDirs.from(
        subprojects.filter { it.name != "front" }.map { proj ->
            proj.layout.buildDirectory.dir("test-results/test")
        }
    )

    val frontProject = project(":front")
    frontTestResultsDir.set(frontProject.layout.buildDirectory.dir("test-results").orElse(
        layout.dir(provider { frontProject.projectDir.resolve("build/test-results") })
    ))

    outputHtml.set(layout.buildDirectory.file("reports/tests.html"))
}
