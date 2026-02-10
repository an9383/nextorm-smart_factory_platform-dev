package com.nextorm.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan("com.nextorm.common.db")
@EnableJpaRepositories("com.nextorm.common.db")
@ComponentScan("com.nextorm")
@SpringBootApplication
class CollectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(CollectorApplication.class, args);
	}
}
