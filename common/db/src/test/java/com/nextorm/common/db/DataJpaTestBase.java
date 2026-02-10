package com.nextorm.common.db;

import com.nextorm.common.db.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

/**
 * Data JPA 테스트 베이스 클래스
 */
@DataJpaTest
@Import(QueryDslConfig.class)
@ContextConfiguration(classes = TestConfiguration.class)
@AutoConfigureJson
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class DataJpaTestBase {
	private static final String DOCKER_IMAGE_NAME = "mariadb:10.6";
	private static final String DATABASE_NAME = "ecopeace_testdb";
	private static final String USERNAME = "testdbuser";
	private static final String PASSWORD = "testdbpass";

	private static final MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>(DOCKER_IMAGE_NAME);

	@BeforeAll
	public static void setUp() {
		mariaDBContainer.withDatabaseName(DATABASE_NAME)
						.withUsername(USERNAME)
						.withPassword(PASSWORD);

		mariaDBContainer.withCommand("--character-set-server=utf8",
			"--collation-server=utf8_bin",
			"--default-authentication-plugin=mysql_native_password",
			"--lower_case_table_names=1",
			"--default-time-zone=Asia/Seoul",
			"--max_allowed_packet=104857600");

		mariaDBContainer.start();
	}

	@DynamicPropertySource
	static void setDatasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mariaDBContainer::getJdbcUrl);
		registry.add("spring.datasource.username", mariaDBContainer::getUsername);
		registry.add("spring.datasource.password", mariaDBContainer::getPassword);
	}
}
