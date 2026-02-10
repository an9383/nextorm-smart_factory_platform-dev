package com.nextorm.failover.executor.zookeeper;

import com.nextorm.failover.config.FailoverConfig;
import com.nextorm.failover.executor.FailoverExecuter;
import com.nextorm.failover.executor.zookeeper.watcherhandler.ZookeeperWatcherEventHandler;
import com.nextorm.failover.handler.FailoverEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import java.util.List;

@Slf4j
public class ZookeeperFailoverExecutor implements ZookeeperWatcherEventHandler, FailoverExecuter, Runnable {
	private final FailoverEventHandler eventHandler;
	private final ZookeeperController zookeeperController;

	/**
	 * Framework System별로 갖는 Unique한 ID 입니다. 동일한 Active가 한개 이상이 구동하는지 확인하기 위한 Flag 역할을 합니다.
	 * 현재 자신이 Active인데, Zookeeper에서 관리하는 UniqueID와 자신의 UniqueID가 다르다면, 다른 Active가 구동된 것이라 판단하고, 자신은 강제 종료 됩니다.
	 */
	private String serviceID = "";
	private Thread checkThread = null;
	private long previousCheckTime = -1;
	private volatile boolean isActiveMode = false;
	private long maxTimeOut = 0;
	private long retryCount = 0;
	private long timeout = 0;
	/**
	 * 여러개의 Standby중 자신이 Active로 전환되어야 함을 인식하기 위한 Flag
	 */
	private boolean haveToChangeToActive = false;
	private ZookeeperFailoverNodeInfo zkNodeInfo;

	public ZookeeperFailoverExecutor(
		FailoverConfig failoverConfig,
		ZookeeperController zookeeperController,
		FailoverEventHandler eventHandler
	) {
		this.zookeeperController = zookeeperController;
		this.eventHandler = eventHandler;

		zkNodeInfo = new ZookeeperFailoverNodeInfo(failoverConfig);

		zookeeperController.addEventHandler(this);
		serviceID = zkNodeInfo.getUuid() + "." + zkNodeInfo.getIp();
		log.info("FAILOVER Zookeeper Node For Framework : " + zkNodeInfo.getZkActiveRootNode());
	}

	@Override
	public void process(WatchedEvent event) {
		//이 이벤트는 사용하지 않음.
		//ZookeeperController에서 Event Callback Function을 직접 호출함.
	}

	/**
	 * 임시 노드가 아닌 영구 노드를 생성합니다.
	 * 존재할 경우에는 생성하지 않고, 존재하지 않을 경우에만 생성됩니다.
	 * 여기서 생성할 때는 Event를 등록하지 않습니다.
	 */
	private void createDefaultNodes() {
		/**
		 * Framework별로 생성되어 관리되는 Zookeeper Node를 정의합니다.
		 * Node 구조는 아래와 같습니다.
		 * ============================================================
		 * FRAMEWORK_TYPE(영구)		SUB.CATEGORY(영구)		FRAMEWORK_NAME(영구)			TASKS(임시)
		 * getSystemTypeName		getActiveName		getZkActiveRootNode				STATUS (주기적 시간 Update 용)
		 * 																																													IP (값으로 PID가 저장됨)
		 * 																											getStandbyNodeName		getZkStandbyRootNode			PID.0000001 (값으로 IP가 저장됨)
		 */
		//	/systemType 노드를 있는지 확인하고 없으면 생성
		checkAndCreateNode(zkNodeInfo.getSystemTypeName(), "framework.system.type", true, false, false);
		//	/systemType/ACTIVE 노드를 있는지 확인하고 없으면 생성
		checkAndCreateNode(zkNodeInfo.getActiveName(), "framework.active.system", true, false, false);
		//	/systemType/STANDBY 노드를 있는지 확인하고 없으면 생성
		checkAndCreateNode(zkNodeInfo.getStandbyName(), "framework.standby.system", true, false, false);
		//	/systemType/ACTIVE/processName 노드를 있는지 확인하고 없으면 생성
		checkAndCreateNode(zkNodeInfo.getZkActiveRootNode(), "framework.active.process", true, false, false);
		//	/systemType/STANDBY/processName 노드를 있는지 확인하고 없으면 생성
		checkAndCreateNode(zkNodeInfo.getZkStandbyRootNode(), "framework.standby.process", true, false, false);
	}

	/**
	 * Zk Node에서 요청한 Node가 있는지 확인하고, 없으면 이를 생성합니다.
	 */
	private String checkAndCreateNode(
		String nodeName,
		String userData,
		boolean isPersistNode,
		boolean isSequential,
		boolean isWatchEvent
	) {
		String createdNodeName = "";
		try {
			if (!zookeeperController.checkNodeExist(nodeName, isWatchEvent)) {
				createdNodeName = zookeeperController.createNode(nodeName,
					userData,
					isPersistNode,
					isSequential,
					isWatchEvent);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return createdNodeName;
	}

	@Override
	public void start() {
		while (!zookeeperController.isConnected()) {
			try {
				Thread.sleep(1);
			} catch (Exception ex) {
			}
		}
		if (zookeeperController.isConnected()) {
			try {
				/**
				 * 기본적으로 생성되어 있어야 할 Node를 확인하고, 없으면 이를 생성한다.
				 */
				createDefaultNodes();
				Thread.sleep(100); //정상 생성될때가지 잠시 대기.
				if (!checkActiveChildExist()) {
					processForActiveFramework();
				} else {
					processForStandbyFramework();
				}
			} catch (NodeExistsException e) {
				// TODO Auto-generated catch block
				if (eventHandler != null) {
					eventHandler.onStartAsStandby();
				}
				e.printStackTrace();
			} catch (ConnectionLossException e) {
				/**
				 * 클라이언트가 주키퍼 서버와의 연결이 끊겼을 때 발생된다.이것은 주로 네트워크 단절과 같은 네트워크 에러나 주키퍼 서버의 장애 상황이다.
				 * 이 예외가 발생하면 클라이언트는 주키퍼 서버가 요청한 작업을 완료하기 전에 연결이 끊긴 것인지 아니면, 작업은 완료 하였지만 클라이언트가 응답을 받지 못한 것인지 알 수 없다.
				 * 보류중인 요청이 잘 처리된 것인지 아니면 다시 요청해야 하는 것인지 판단이 필요하다.
				 */
				if (eventHandler != null) {
					eventHandler.onErrorOccurs("start", e);
				}
				throw new RuntimeException(e);
			} catch (KeeperException e) {
				if (eventHandler != null) {
					eventHandler.onErrorOccurs("start", e);
				}
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else {
			if (this.eventHandler != null) {
				this.eventHandler.onDisconnected();
			}
		}
	}

	/**
	 * 현재 Node Name으로 Active되어있는 System이 구동 중인지 확인합니다.
	 *
	 * @return : Active Node가 존재하면 Return true, 없으면 False를 리턴합니다.
	 */
	private boolean checkActiveChildExist() {
		boolean isExist = false;
		checkAndCreateNode(zkNodeInfo.getZkActiveRootNode(), "", true, false, false);

		try {
			List<String> childList = zookeeperController.getChildren(zkNodeInfo.getZkActiveRootNode(), true);
			/**
			 * 생성된 Active가 없다면
			 */
			if (childList == null || childList.isEmpty()) {
				isExist = false;
			} else {
				isExist = true;
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		}

		return isExist;
	}

	/**
	 * System Node가 이미 존재할 경우에 대한 처리로직이다.
	 *
	 * @throws KeeperException
	 */
	private void processForStandbyFramework() throws KeeperException {
		/**
		 * Node가 존재한다는 것은 Active System이 이미 구동 중이라는 이야기다.
		 * 우선, 기 등록된 System이 자신인지 확인한다.
		 */
		String remoteServiceID = zookeeperController.getNodeData(zkNodeInfo.getZkActiveSystemNode(), false);
		//	/systemType/ACTIVE/processName/ACTIVE.System 노드값과 serviceID를 비교
		if (remoteServiceID.equals(this.serviceID)) {
			/**
			 * 자신이 등록된 것이므로, Active Mode로 구동되면 된다. (그럴일은 없겠지만..)
			 */
			startActiveThread();
			if (eventHandler != null) {
				eventHandler.onStartAsActive();
			}
			//FrameworkController.getInstance().doAsActive();
		} else {
			/**
			 * Zookeeper의 대기 Node에 자신을 등록한다.
			 * 대기 Node는 Sequential 하게 등록되어야 한다.
			 */
			registStandbyFrameworkWaitNode();

			/**
			 * 현재 다른 Active가 구동중이다.
			 * Service ID도 나와 다르다.
			 * Active System이 Status Update도 잘 하고 있다.
			 * 따라서 나는 Standby로 구동되어야 한다.
			 */
			startStandbyThread();
			if (eventHandler != null) {
				eventHandler.onStartAsStandby();
			}
		}
	}

	/**
	 * System이 Standby Mode로 구동 될 경우, 자신이 대기중이라는 것을 등록한다.
	 * 하나 이상의 Standby 가 구동될 경우, 가장 먼저 등록한 것이 향후에 Active로 구동되어야 한다.
	 * 따라서, 대기 Node는 Sequential Node로 등록해야 한다.
	 */
	private void registStandbyFrameworkWaitNode() {
		try {
			String retNodeName = "";
			// isInvokeEvent가 true일때는 Wather의 감시를 수행
			retNodeName = checkAndCreateNode(zkNodeInfo.getZkStandbyNode(), zkNodeInfo.getIp(), false, true, true);
			if (retNodeName != null && !retNodeName.equals("NONE") && !retNodeName.equals("")) {
				zkNodeInfo.setZkStandbyNodeSeq(retNodeName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeStandbyFrameworkWaitNode() {
		try {
			zookeeperController.deleteNode(zkNodeInfo.getZkStandbyNodeSeq(), false);
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 해당 System의 Node가 존재하지 않은 경우를 위한 로직이다.
	 */
	private void processForActiveFramework() {

		/**
		 Node가 존재하지 않는다는 것은, 아직 Active가 구동되지 않는다는 것.
		 우선, 자신의 노드 및 Status Node를 생성한다.
		 */
		try {
			//	/systemType/ACTIVE/processName/ACTIVE.System
			String retNodeName = zookeeperController.createNode(zkNodeInfo.getZkActiveSystemNode(),
				serviceID,
				false,
				false,
				true);
			if (retNodeName.equals(zkNodeInfo.getZkActiveSystemNode())) {
				try {
					//	/systemType/ACTIVE/processName/ACTIVE.Status
					zookeeperController.createNode(zkNodeInfo.getZkActiveStatusNode(),
						buildStatusData(),
						false,
						false,
						false);
					//	/systemType/ACTIVE/processName/ACTIVE.IP
					zookeeperController.createNode(zkNodeInfo.getZkActiveIPNode(),
						zkNodeInfo.getIp(),
						false,
						false,
						false);
					//	/systemType/ACTIVE/processName/ACTIVE.PID
					zookeeperController.createNode(zkNodeInfo.getZkActiveUUIDNode(),
						zkNodeInfo.getUuid(),
						false,
						false,
						false);
					startActiveThread();
					if (eventHandler != null) {
						eventHandler.onStartAsActive();
					}
				} catch (KeeperException e) {
					e.printStackTrace();
				}
			}
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 비 정상적인 Active Node를 제거한다.
	 */
	private void removeActiveNode() {

		try {
			zookeeperController.deleteNode(zkNodeInfo.getZkActiveStatusNode(), false);
			zookeeperController.deleteNode(zkNodeInfo.getZkActiveIPNode(), false);
			zookeeperController.deleteNode(zkNodeInfo.getZkActiveUUIDNode(), false);
			zookeeperController.deleteNode(zkNodeInfo.getZkActiveSystemNode(), true);
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	private void startStandbyThread() {
		isActiveMode = false;
		if (checkThread != null) {
			if (checkThread.isAlive()) {
				checkThread.interrupt();
				try {
					checkThread.join();
				} catch (InterruptedException e) {
					Thread.currentThread()
						  .interrupt();
					throw new RuntimeException(e);
				}
			}
			checkThread = null;
		}

		checkThread = new Thread(this);
		checkThread.setName("Failover.TStandbyMode");
		checkThread.start();

	}

	/**
	 * System이 Active Mode로 구동되었을 때, 주기적으로 Check해야 하는 작업을 진행하기 위한 Thread 이다.
	 */
	private void startActiveThread() {
		isActiveMode = true;
		if (checkThread != null) {
			if (checkThread.isAlive()) {
				checkThread.interrupt();
				try {
					checkThread.join();
				} catch (InterruptedException e) {
					log.error("InterruptedException occurred: " + e.getMessage());
					Thread.currentThread()
						  .interrupt();
					// throw new RuntimeException(e);
				}
			}
			checkThread = null;
		}

		checkThread = new Thread(this);
		checkThread.setName("Failover.TActiveMode");
		checkThread.start();
	}

	/////////////////////// [ Thread 처리 로직 ] ////////////////////

	@Override
	public void run() {
		if (Thread.currentThread()
				  .getName()
				  .equals("Failover.TActiveMode")) {
			doActiveThreadProcess();
		} else {
			doStandbyThreadProcess();
		}
	}

	/**
	 * Active System Node가 살아 있는지만 확인하고,
	 * 만일 삭제되었다면, 자신의 상태를 확인한 후, Active로 전환한다.
	 */
	private void doStandbyThreadProcess() {
		log.info("FAILOVER Failover Check Thread is Started : " + Thread.currentThread()
																		.getName());
		try {
			while (!isActiveMode && !Thread.currentThread()
										   .isInterrupted()) {
				try {
					if (!zookeeperController.checkNodeExist(zkNodeInfo.getZkActiveUUIDNode(),
						false)) { //Active Node가 제거됨.
						if (checkMyStandbyOrder()) { //자신이 대기중인 Standby Framework 중 가장 우선순위가 높은지 확인한다.
							break;
						}
					} else {
						//System.out.println(System.currentTimeMillis() + " : Active Node Exist");
					}
					Thread.sleep(1000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("FAILOVER Failover Check for Active Thread is Stopped : " + Thread.currentThread()
																				   .getName());
		changeToActiveMode();
	}

	private boolean checkMyStandbyOrder() {
		try {
			long maxValue = -1;
			long tempValue = -1;
			int maxValueIndex = -1;
			String nodeName = "";
			List<String> childList = zookeeperController.getChildren(zkNodeInfo.getZkStandbyRootNode(), false);
			if (childList != null && !childList.isEmpty()) {
				String[] nodeDatas = null;
				for (int i = 0; i < childList.size(); i++) {
					String childNode = childList.get(i);
					//Standby.b1c09a17.0000000004 이런 형식으로 저장된다.
					//마지막 3번째 숫자중 가장 큰 값과 자신을 비교해서, 동일하다면 자신이 Active가 된다.
					nodeDatas = childNode.split("\\.");
					if (nodeDatas.length >= 3) {
						tempValue = Long.parseLong(nodeDatas[2]);
						if (tempValue > maxValue) {
							maxValue = tempValue;
							maxValueIndex = i;
						}
					} else {
						log.info("Standby Node Name is invalid : " + tempValue);
					}
				}
				if (maxValue > -1 && maxValueIndex > -1) {
					nodeName = childList.get(maxValueIndex);
					if (zkNodeInfo.getZkStandbyNodeSeq()
								  .equals(zkNodeInfo.getZkStandbyRootNode() + "/" + nodeName)) {
						haveToChangeToActive = true;
					} else {
						haveToChangeToActive = false;
					}
				} else {
					log.info("cannot find available standby node");
					haveToChangeToActive = true;
				}
			} else {
				log.info("There is not exist standby nodes");
				haveToChangeToActive = true;
			}
		} catch (KeeperException e) {

		}
		return haveToChangeToActive;
	}

	/**
	 * Standby System에서 Active System을 확인한 결과 Active System에 문제가 있다고 판단할 경우, 강제로 Active로 변환하기 위한 기능 구현
	 */
	private void changeToActiveMode() {

		if (!checkNodeServiceID()) {
			removeActiveNode();
		}

		isActiveMode = true;
		processForActiveFramework();
	}

	private String buildStatusData() {
		return "" + System.currentTimeMillis();
	}

	/**
	 * System이 Active Mode로 구동되었을 때, 주기적으로 Check 해야 하는 기능 입니다.
	 */
	private void doActiveThreadProcess() {
		log.info("FAILOVER Failover Check Thread is Started : " + Thread.currentThread()
																		.getName());
		try {
			/**
			 * 만일 자신의 이름으로 Standby Node가 생성되어 있다면 이를 제거한다.
			 */
			int waitTodeleteCount = 0; //Standby Node를 바로 삭제할 경우, 다른 Standby가 Active로 전환될 수 있으므로, 3회 Status update 한 이후 삭제한다.
			while (isActiveMode && !Thread.currentThread()
										  .isInterrupted()) {
				try {

					if (!checkNodeServiceID()) { //주기적으로 Node의 존재 여부 및 자신의 Service ID와 동일한지 검사한다. 이는 Active System이 Hang이 되어, Standby System이 Active가 되었는지 검사하기 위함이다.
						log.info("FAILOVER because of another system is start as active, this system will be down");
						break;
					}
					updateStatus();    //  /systemType/ACTIVE/prooceName/Active.Status를 현시간 시간 값으로 업데이트
					Thread.sleep(1000);
					/**
					 * Standby에서 Active로 전환된 후, 1회이상은 Status를 Update 한 후에 자신의 Sequence Node를 삭제해야 한다.
					 * 그렇지 않으면, 다른 Standby System이 Active로 전환될 수 있다.
					 */
					if (zkNodeInfo.getZkStandbyNodeSeq()
								  .length() > 0) {
						waitTodeleteCount++;
						if (waitTodeleteCount > 3) {
							removeStandbyFrameworkWaitNode();
							zkNodeInfo.setZkStandbyNodeSeq("");
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("FAILOVER Failover Check Thread is Stopped : " + Thread.currentThread()
																		.getName());
		System.exit(-1);
	}

	private boolean checkNodeServiceID() {
		boolean isAvailable = false;
		try {
			if (zookeeperController.checkNodeExist(zkNodeInfo.getZkActiveSystemNode(), false)) {
				String nodeData = zookeeperController.getNodeData(zkNodeInfo.getZkActiveSystemNode(), false);
				if (nodeData.equals(this.serviceID)) {
					isAvailable = true;
				}
			}
		} catch (ConnectionLossException ex) {
			isAvailable = true;
			ex.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
		return isAvailable;
	}

	private void updateStatus() {
		String data = "";
		try {
			data = buildStatusData();
			if (!zookeeperController.checkNodeExist(zkNodeInfo.getZkActiveStatusNode(), false)) {
				zookeeperController.createNode(zkNodeInfo.getZkActiveStatusNode(), data, false, false, false);
			}
			zookeeperController.setNodeData(zkNodeInfo.getZkActiveStatusNode(), data, -1);
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}
	//////////////////////// [ Zookeeper 관련 Event 처리 로직] ///////////////////////

	@Override
	public void onSyncConnected(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onSyncConnected [" + path + "]");
		if (eventHandler != null) {
			eventHandler.onConnected();
		}
	}

	@Override
	public void onExpired(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onExpired [" + path + "]");
		if (eventHandler != null) {
			eventHandler.onDisconnected();
		}
	}

	@Override
	public void onAuthFailed(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onAuthFailed [" + path + "]");
		if (eventHandler != null) {
			eventHandler.onDisconnected();
		}
	}

	@Override
	public void onConnectedReadOnly(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onConnectedReadOnly [" + path + "]");
	}

	@Override
	public void onDisconnected(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onDisconnected [" + path + "]");
		if (eventHandler != null) {
			eventHandler.onDisconnected();
		}
	}

	@Override
	public void onSaslAuthenticated(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onSaslAuthenticated [" + path + "]");
	}

	@Override
	public void onUnknown(
		String path,
		EventType type
	) {

		log.info("FAILOVER Event Invoked : onUnknown [" + path + "]");
	}

	@Override
	public void onNodeChildChanged(String path) {

		log.info("FAILOVER Event Invoked : onNodeChildChanged [" + path + "]");
	}

	@Override
	public void onNodeCreated(String path) {

		log.info("FAILOVER Event Invoked : onNodeCreated [" + path + "]");
	}

	@Override
	public void onNodeDataChanged(String path) {

		log.info("FAILOVER Event Invoked : onNodeDataChanged [" + path + "]");
	}

	@Override
	public void onNodeDeleted(String path) {

		log.info("FAILOVER Event Invoked : onNodeDeleted [" + path + "]");
		/**
		 * 현재 Standby Mode로 구동되고 있고, 수신된 Path가 본인의 Node와 동일하다면
		 * 현재 상태를 Active Mode로 전환한다.
		 */
		if (!this.isActiveMode && path.equals(zkNodeInfo.getZkActiveSystemNode()) && haveToChangeToActive) {
			isActiveMode = true;
			processForActiveFramework();
		}
	}

}

