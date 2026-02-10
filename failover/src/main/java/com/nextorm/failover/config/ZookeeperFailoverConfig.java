package com.nextorm.failover.config;

import com.nextorm.failover.executor.zookeeper.ZookeeperController;
import com.nextorm.failover.executor.zookeeper.ZookeeperHandler;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.io.IOException;

@Configuration
public class ZookeeperFailoverConfig {
	@Bean
	public ZooKeeper zooKeeper(FailoverConfig failoverConfig) throws IOException {
		if (!failoverConfig.isUse()) {
			return null;
		}
		return new ZooKeeper(failoverConfig.getZookeeperHosts(),
			failoverConfig.getZookeeperConnectionTimeout(),
			(event) -> {
			});
	}

	@Bean
	public ZookeeperHandler zookeeperHandler(@Nullable ZooKeeper zooKeeper) {
		if (zooKeeper == null) {
			return null;
		}
		return new ZookeeperHandler(zooKeeper);
	}

	@Bean
	public ZookeeperController zookeeperController(@Nullable ZookeeperHandler zookeeperHandler) {
		if (zookeeperHandler == null) {
			return null;
		}
		return new ZookeeperController(zookeeperHandler);
	}
}
