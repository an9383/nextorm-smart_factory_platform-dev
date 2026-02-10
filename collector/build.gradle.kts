plugins {

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    implementation("org.json:json:20240303")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.jayway.jsonpath:json-path:2.9.0")

    implementation(project(":common:define"))
    api(project(":common:db"))
    implementation(project(":failover"))
    implementation("org.mongodb:mongodb-driver-sync:4.11.1")
    implementation("com.ghgande:j2mod:3.1.0")
    implementation("org.eclipse.milo:sdk-client:0.6.16")
    implementation("org.eclipse.milo:sdk-server:0.6.16")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}