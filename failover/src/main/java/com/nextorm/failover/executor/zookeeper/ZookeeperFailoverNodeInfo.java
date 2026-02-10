package com.nextorm.failover.executor.zookeeper;

import com.nextorm.failover.config.FailoverConfig;
import com.nextorm.failover.oscommand.AbstractOSCommand;
import com.nextorm.failover.oscommand.CreateUuid;
import lombok.Getter;
import lombok.Setter;

/**
 * Zookeeper Node 구성 형태
 * SENSOR_FRAMEWORK						: 영구 노드
 * GROUPS								 영구 노드
 * ACTIVE						: 영구 노드 (CONSTANT)
 * FW01						: 영구 노드
 * Active.Framework 	: 임시 노드
 * Active.Status			: 임시 노드
 * Active.UUID				: 임시 노드
 * Active.IP				: 임시 노드
 * FW02						: 영구 노드
 * STANDBY						: 영구 노드 (CONSTANT)
 * FW01						: 영구 노드
 * Standby.UUID.SeqNo : 순차적 임시 노드
 * FW02						: 영구 노드
 * GROUP_NAME2						: 영구 노드
 * SCHEDULER
 */

@Getter
@Setter
public class ZookeeperFailoverNodeInfo {
	private String systemType;
	private String processName;
	private String ip;
	private String uuid;
	private String zkActiveRootNode;
	private String zkStandbyRootNode;
	private String zkActiveStatusNode;
	private String zkActiveSystemNode;
	private String zkStandbyNode;
	private String zkActiveIPNode;
	private String zkActiveUUIDNode;

	/**
	 * Standby Node는 Sequential Node로 생성된다. 따라서 Node 이름이 생성시 변경되므로, 그 값을 관리해야 한다.
	 */
	private String zkStandbyNodeSeq = "";

	public ZookeeperFailoverNodeInfo(
		FailoverConfig failoverConfig
	) {

		AbstractOSCommand commander = AbstractOSCommand.getFrameworkCommander();
		this.systemType = failoverConfig.getSystemType();
		this.processName = failoverConfig.getProcessName();
		this.ip = failoverConfig.getSystemIp();
		this.uuid = CreateUuid.createShortUuid();

		/*
		  zk의 해당 Framework의 Root Node 이름입니다.
		 */
		this.zkActiveRootNode = "/" + systemType + "/ACTIVE/" + processName;

		/*
		  영구 노드로, 각 Framework 별로 생성된다.
		  Framework 별로, Standby로 대기하는 Node가 하위에 등록되며, 만일 해당 Framework이 종료되거나, Active로 전환되면, 해당 노드에서 제거된다.
		 */
		this.zkStandbyRootNode = "/" + systemType + "/STANDBY/" + processName;

		/*
		  현재 Framework에 대한 상태를 저장하기 위한 Node 입니다. 여기에는 최근 Update한 시간이 등록됩니다.
		 */
		this.zkActiveStatusNode = zkActiveRootNode + "/Active.Status";

		/*
		  Active Mode로 구동중인 Framework의 IP를 저장한 Node 입니다.
		 */
		this.zkActiveSystemNode = zkActiveRootNode + "/Active.System";

		/*
		  Standby Framework으로 구동되기 위한 Node 이름입니다.
		 */
		this.zkStandbyNode = zkStandbyRootNode + "/Standby." + uuid + ".";

		this.zkActiveIPNode = zkActiveRootNode + "/Active.IP";

		this.zkActiveUUIDNode = zkActiveRootNode + "/Active.UUID";
	}

	/**
	 * 현재 Node가 Standby Mode로 구동될 경우, Standby Node에 등록해야 합니다.
	 * Standby Node는 Sequential하게 등록되므로, 등록되는 순간 이름이 추가되어 생성됩니다.
	 * 이 값을 관리하기 위해서 주키퍼로부터 리턴된 이름을 저장합니다.
	 */
	public void setZkStandbyNodeSeq(String zkStandbyNodeSeq) {
		this.zkStandbyNodeSeq = zkStandbyNodeSeq;
	}

	public String getSystemTypeName() {
		return "/" + systemType;
	}

	public String getActiveName() {
		return "/" + systemType + "/ACTIVE";
	}

	public String getStandbyName() {
		return "/" + systemType + "/STANDBY";
	}
}
