package com.nextorm.extensions.proxystatusgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProxyStatusGatewayApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProxyStatusGatewayApplication.class, args);
	}
}
