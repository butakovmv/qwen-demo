plugins {
    base
}

val npmInstall by tasks.registering(Exec::class) {
    group = "build"
    description = "Install ui-test dependencies"
    workingDir = projectDir
    commandLine("npm", "ci")
}

val uiTest by tasks.registering(Exec::class) {
    group = "verification"
    description = "Run Playwright smoke tests against running application"
    workingDir = projectDir
    commandLine("npx", "playwright", "test")
    dependsOn(npmInstall)
}

val uiTestDocker by tasks.registering {
    group = "verification"
    description = "Run Playwright smoke tests in Docker Compose"
    dependsOn(":app:bootJar", ":front:tarDist")
    finalizedBy(":ui-test:dockerComposeUp")
}

tasks.register<Exec>("dockerComposeUp") {
    group = "verification"
    description = "Run docker compose with smoke tests"
    workingDir = rootDir
    commandLine("docker", "compose", "up", "--build", "smoke-test")
}
