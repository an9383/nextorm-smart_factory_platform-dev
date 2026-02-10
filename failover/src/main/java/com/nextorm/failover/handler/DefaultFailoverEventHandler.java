package com.nextorm.failover.handler;

import com.nextorm.failover.ProjectStartupEntryPoint;
import com.nextorm.failover.config.FailoverConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultFailoverEventHandler implements FailoverEventHandler {
	private final FailoverConfig failoverConfig;
	private final String uuid;
	private final List<String> myIPAddressList;
	private final List<ProjectStartupEntryPoint> startupEntryPoints;

	private String role = "down";

	public DefaultFailoverEventHandler(
		FailoverConfig failoverConfig,
		String uuid,
		List<String> myIPAddressList,
		List<ProjectStartupEntryPoint> startupEntryPoints
	) {
		this.failoverConfig = failoverConfig;
		this.uuid = uuid;
		this.myIPAddressList = myIPAddressList;
		this.startupEntryPoints = startupEntryPoints;
	}

	@Override
	public void onStatusChanged() {
		log.info("FAILOVER Framework Status Changed : {}", role);
	}

	@Override
	public void onStartAsActive() {

		role = "active";
		log.info("FAILOVER Framework Status Changed to Active Mode");
		log.info("========= [ " + failoverConfig.getSystemType() + " STARTED AS ACTIVE MODE ] ==========");
		log.info("========= [ " + failoverConfig.getSystemType() + " STARTED AS ACTIVE MODE ] ==========");
		log.info(">> PROCESS ID : " + this.uuid);
		for (String IP : this.myIPAddressList) {
			log.info(">> MACHINE IP ADDRESS : " + IP);
		}
		log.info(">> SYSTEM TYPE : " + failoverConfig.getSystemType());
		log.info(">> " + failoverConfig.getSystemType() + " Process Name : " + failoverConfig.getProcessName());
		log.info("===================================================");
		startupEntryPoints.forEach(ProjectStartupEntryPoint::start);
	}

	@Override
	public void onStartAsStandby() {

		role = "standby";
		log.info("FAILOVER Framework Status Changed to Standby Mode");
		log.info("========= [ FRAMEWORK STARTED AS STANDBY MODE ] ==========");
		log.info("========= [ FRAMEWORK STARTED AS STANDBY MODE ] ==========");
		log.info(">> PROCESS ID : " + this.uuid);
		for (String IP : this.myIPAddressList) {
			log.info(">>  MACHINE IP ADDRESS : " + IP);
		}
		log.info("====================================================");
	}

	@Override
	public void onDisconnected() {

		//		role = DEFINE_CONSTANT.FrameworkRole.DOWN;
		//		writeRunLog("Due to Establish the Failover connection fail, Cannot Start Framework");
		//		try {Thread.sleep(5000);} catch (InterruptedException e1) {}
		//		System.exit(-1);
	}

	@Override
	public void onConnected() {

	}

	@Override
	public void onErrorOccurs(
		String method,
		Exception ex
	) {

		ex.printStackTrace();
	}
}
