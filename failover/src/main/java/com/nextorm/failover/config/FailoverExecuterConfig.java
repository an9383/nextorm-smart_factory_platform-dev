package com.nextorm.failover.config;

import com.nextorm.failover.ProjectStartupEntryPoint;
import com.nextorm.failover.executor.FailoverExecuter;
import com.nextorm.failover.executor.NonFailoverExecutor;
import com.nextorm.failover.executor.zookeeper.ZookeeperController;
import com.nextorm.failover.executor.zookeeper.ZookeeperFailoverExecutor;
import com.nextorm.failover.handler.DefaultFailoverEventHandler;
import com.nextorm.failover.handler.FailoverEventHandler;
import com.nextorm.failover.oscommand.AbstractOSCommand;
import com.nextorm.failover.oscommand.CreateUuid;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.List;

@Configuration
public class FailoverExecuterConfig {

	@Bean
	public FailoverEventHandler failoverEventHandler(
		FailoverConfig failoverConfig,
		List<ProjectStartupEntryPoint> startupEntryPoints
	) {
		AbstractOSCommand commander = AbstractOSCommand.getFrameworkCommander();
		String uuid = CreateUuid.createShortUuid();

		return new DefaultFailoverEventHandler(failoverConfig, uuid, commander.getMyIPAddress(), startupEntryPoints);
	}

	@Bean
	public FailoverExecuter failoverExecuter(
		FailoverConfig failoverConfig,
		FailoverEventHandler failoverEventHandler,
		@Nullable ZookeeperController zookeeperController
	) {
		if (failoverConfig.isUse()) {
			return new ZookeeperFailoverExecutor(failoverConfig, zookeeperController, failoverEventHandler);
		}
		return new NonFailoverExecutor(failoverEventHandler);
	}
}
