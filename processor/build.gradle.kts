plugins {

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // script engine
    implementation("org.graalvm.js:js:23.0.3")
    implementation("org.graalvm.js:js-scriptengine:23.1.2")

    // jackson
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation(project(":failover"))
    implementation("org.apache.zookeeper:zookeeper:3.9.2")
    api(project(":common:db"))

    //httpclient5
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.3")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.3.3")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}