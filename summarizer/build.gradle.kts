plugins {

}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(project(":failover"))
    implementation("org.apache.zookeeper:zookeeper:3.9.2")
    implementation(project(":common:db"))
}


tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}
