plugins {

}

dependencies {
    implementation("org.apache.zookeeper:zookeeper:3.9.2")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}
