plugins {
    base
}

val npmInstall by tasks.registering(Exec::class) {
    group = "build"
    description = "Install npm dependencies"
    workingDir = projectDir
    commandLine("npm", "ci")
}

val npmBuild by tasks.registering(Exec::class) {
    group = "build"
    description = "Build frontend with Vite"
    workingDir = projectDir
    commandLine("npm", "run", "build")
    dependsOn(npmInstall)
}

tasks.register<Exec>("npmLint") {
    group = "verification"
    description = "Run ESLint for frontend"
    workingDir = projectDir
    commandLine("npm", "run", "lint:check")
    dependsOn(npmInstall)
}

tasks.register<Exec>("npmFormatCheck") {
    group = "verification"
    description = "Run Prettier check for frontend"
    workingDir = projectDir
    commandLine("npm", "run", "format:check")
    dependsOn(npmInstall)
}

tasks.register<Exec>("npmFormatFix") {
    group = "formatting"
    description = "Run Prettier with auto-fix for frontend"
    workingDir = projectDir
    commandLine("npm", "run", "format")
}

tasks.register<Exec>("test") {
    group = "verification"
    description = "Run vitest for frontend"
    workingDir = projectDir
    commandLine("npm", "run", "test")
    dependsOn(npmInstall)
}

val tarDist by tasks.registering(Tar::class) {
    group = "build"
    description = "Package frontend dist into tar archive"
    archiveFileName.set("front-dist.tar")
    destinationDirectory.set(layout.buildDirectory)
    from("dist")
    dependsOn(npmBuild)
}

tasks.assemble {
    dependsOn(tarDist)
}
