package com.nextorm.portal;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(value = {"com.nextorm.common.db"})
@EntityScan(basePackages = {"com.nextorm.common.db", "com.nextorm.portal.entity"})
@EnableJpaRepositories(basePackages = {"com.nextorm.common.db", "com.nextorm.portal.repository"})
@SpringBootApplication
public abstract class TestConfiguration {

}