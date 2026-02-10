dependencies {
    // WebFlux for reactive programming and high performance (Jackson 포함)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Validation for configuration validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Test dependencies
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.assertj:assertj-core")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}