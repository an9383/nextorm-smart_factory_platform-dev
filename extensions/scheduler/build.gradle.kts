plugins {

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")

    implementation("com.ghgande:j2mod:3.1.0")
    implementation("de.siegmar:fastcsv:4.0.0")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}