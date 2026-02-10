plugins {

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    api(project(":common:db"))
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}