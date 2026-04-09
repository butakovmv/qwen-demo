plugins {
    kotlin("jvm")
    id("io.spring.dependency-management")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(project(":operation"))
    implementation(project(":postgres"))
    implementation(project(":web-api"))
    implementation(project(":app"))

    // Spring dependencies needed for annotation checks
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")

    // Kotlin reflection for visibility checks
    implementation(kotlin("reflect"))

    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
