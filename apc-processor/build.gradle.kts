plugins {

}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // script engine
    implementation("org.graalvm.js:js:23.0.3")
    implementation("org.graalvm.js:js-scriptengine:23.1.2")

    // slack api
    implementation("com.slack.api:slack-api-client:1.44.1")

    api(project(":common:db"))
    api(project(":common:apc"))
    testRuntimeOnly("com.h2database:h2")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}