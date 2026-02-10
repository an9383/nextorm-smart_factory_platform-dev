package com.nextorm.apcmodeling;

import com.nextorm.common.db.config.QueryDslConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Import({QueryDslConfig.class})
@EntityScan(basePackages = {"com.nextorm.common.apc.entity", "com.nextorm.common.db.entity"})
@EnableJpaRepositories(basePackages = {"com.nextorm.common.apc.repository", "com.nextorm.common.db.repository"})
@SpringBootApplication(scanBasePackages = "com.nextorm")
public class ApcModelingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApcModelingApplication.class, args);
	}

}
