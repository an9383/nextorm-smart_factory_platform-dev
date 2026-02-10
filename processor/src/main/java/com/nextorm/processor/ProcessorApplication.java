package com.nextorm.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EntityScan({"com.nextorm.common.db", "com.nextorm.processor.ocap.user"})
@EnableJpaRepositories({"com.nextorm.common.db", "com.nextorm.processor.ocap.user"})
@ComponentScan("com.nextorm")
@SpringBootApplication
public class ProcessorApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProcessorApplication.class, args);
	}
}
