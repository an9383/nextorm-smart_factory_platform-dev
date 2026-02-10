// Debezium 핵심 의존성
val debeziumVersion = "2.7.3.Final"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    implementation("io.debezium:debezium-api:$debeziumVersion")
    implementation("io.debezium:debezium-embedded:$debeziumVersion")
    implementation("io.debezium:debezium-connector-mariadb:$debeziumVersion")
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = true
}