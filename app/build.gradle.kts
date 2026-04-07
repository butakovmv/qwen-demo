import java.io.ByteArrayOutputStream
import java.time.Instant

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":operation"))
    implementation(project(":web-api"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

springBoot {
    buildInfo()
}

tasks.register("generateGitInfo") {
    description = "Generate git.properties from current git state"
    doLast {
        val gitPropertiesFile = file("build/generated/resources/git.properties")
        gitPropertiesFile.parentFile.mkdirs()

        fun runGit(vararg args: String): String =
            try {
                val output = ByteArrayOutputStream()
                project.exec {
                    workingDir = rootDir
                    commandLine = listOf(*args)
                    standardOutput = output
                    isIgnoreExitValue = true
                }
                val result = output.toString().trim()
                if (result.isNotEmpty()) result else "unknown"
            } catch (e: Exception) {
                "unknown"
            }

        val branch = runGit("git", "branch", "--show-current")
        val commit = runGit("git", "log", "-1", "--format=%H")
        val commitTime = runGit("git", "log", "-1", "--format=%cI")

        val props = buildString {
            append("git.branch=").appendLine(if (branch != "unknown" && branch.isNotBlank()) branch else "HEAD")
            append("git.commit.id=").appendLine(if (commit.isNotEmpty() && commit != "unknown") commit else "unknown")
            append("git.commit.time=").appendLine(if (commitTime.isNotEmpty() && commitTime != "unknown") commitTime else Instant.now().toString())
        }

        gitPropertiesFile.writeText(props)
    }
}

tasks.bootJar {
    dependsOn("generateGitInfo")
    from("build/generated/resources")
}
