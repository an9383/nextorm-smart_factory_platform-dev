package com.nextorm.failover.executor.zookeeper;

import com.nextorm.failover.executor.zookeeper.watcherhandler.AbstractZookeeperWatcherEventHandler;
import com.nextorm.failover.executor.zookeeper.watcherhandler.ZookeeperWatcherEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ZookeeperController extends AbstractZookeeperWatcherEventHandler {
	private final ZookeeperHandler handler;

	private boolean isConnected = false;
	private final ArrayList<ZookeeperWatcherEventHandler> eventHandlerList = new ArrayList<>();

	public ZookeeperController(ZookeeperHandler zookeeperHandler) {
		this.handler = zookeeperHandler;
		this.handler.addWatcher(this);
	}

	public void addEventHandler(ZookeeperWatcherEventHandler eventhandler) {
		eventHandlerList.add(eventhandler);
	}

	/**
	 * Zookeeper Host와의 연결이 구성되어 있는지 확인합니다.
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * 새로운 zk Node를 생성합니다.
	 */
	public String createNode(
		String nodeName,
		String userData,
		boolean isPersistNode,
		boolean isSequential,
		boolean isWatchEvent
	) throws KeeperException {
		String retVal = "NONE";
		if (handler != null) {
			try {
				if (!handler.isExistNode(nodeName, false)) {
					log.info("ZOOKEEPER Try to Create Zookeeper Node for " + nodeName);
					retVal = handler.createNode(nodeName, userData, isPersistNode, isSequential, isWatchEvent);
					log.info("ZOOKEEPER Zookeeper Node for " + retVal + " is Created");
				} else {
					log.info("ZOOKEEPER Requested Node already Exist : " + nodeName);
				}
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
			}
		}
		return retVal;
	}

	public boolean deleteNode(
		String nodeName,
		boolean isWatchEvent
	) throws KeeperException {
		boolean retVal = false;
		if (handler != null) {
			try {
				handler.deleteNode(nodeName, isWatchEvent);
				log.info("Zookeeper Node for " + nodeName + " is deleted");
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
				//				isConnected = false;
				//				close();
				//				retVal = false;
			}
		}
		return retVal;
	}

	public boolean checkNodeExist(
		String nodeName,
		boolean isWatchEvent
	) throws KeeperException {
		boolean retVal = false;
		if (handler != null) {
			try {
				retVal = handler.isExistNode(nodeName, isWatchEvent);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
				//				isConnected = false;
				//				retVal = false;
				//				close();
			}
		}
		return retVal;
	}

	public List<String> getChildren(
		String nodeName,
		boolean isWatchEvent
	) throws KeeperException {
		List<String> list = null;
		if (handler != null) {
			try {
				return handler.getChildren(nodeName, isWatchEvent);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<String> getChildren(
		String nodeName,
		boolean isWatchEvent,
		Stat stat
	) throws KeeperException, InterruptedException {
		List<String> list = null;
		if (handler != null) {
			try {
				return handler.getChildren(nodeName, isWatchEvent, stat);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<String> getChildren(
		String nodeName,
		Watcher watcher
	) throws KeeperException, InterruptedException {
		List<String> list = null;
		if (handler != null) {
			try {
				return handler.getChildren(nodeName, watcher);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
			}
		}
		return list;
	}

	public List<String> getChildren(
		String nodeName,
		Watcher watcher,
		Stat stat
	) throws KeeperException, InterruptedException {
		List<String> list = null;
		if (handler != null) {
			try {
				return handler.getChildren(nodeName, watcher, stat);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
			}
		}
		return list;
	}

	public void checkNodeExistAsync(
		String nodeName,
		Watcher watcher,
		StatCallback callback
	) throws KeeperException {
		if (handler != null) {
			handler.checkNodeAsync(nodeName, watcher, callback, null);
		}
	}

	public void close() {
		log.info("ZOOKEEPER Try to close Zookeeper connection");
		if (handler != null) {
			try {
				handler.stop();
				isConnected = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public String getNodeData(
		String path,
		boolean isWatchEvent
	) throws KeeperException {
		String retVal = "";
		if (handler != null) {
			try {
				retVal = handler.getNodeData(path, isWatchEvent);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
				//				isConnected = false;
				//				close();
			}
		}
		return retVal;
	}

	/**
	 * 특정 노드의 데이터를 수정합니다
	 *
	 * @param path    : 대상 노드 정보 입니다.
	 * @param data    : 저장할 데이터 입니다.
	 * @param version : 저장할 노드의 버전 입니다. 이 버전이 다를 경우, 저장되지 않습니다.
	 * @return
	 * @throws KeeperException
	 */
	public int setNodeData(
		String path,
		String data,
		int version
	) throws KeeperException {
		int retVal = 0;
		if (handler != null) {
			try {
				retVal = handler.setNodeData(path, data, version);
			} catch (InterruptedException e) {
				log.error("ZOOKEEPER Zookeeper occurs Thread Interrupt Exception, So Remove Zookeeper Instance", e);
				e.printStackTrace();
				//				isConnected = false;
				//				close();
			}
		}
		return retVal;
	}

	@Override
	public void onSyncConnected(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onSyncConnected [" + path + "]");
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onSyncConnected(path, type);
			}
		}
		isConnected = true;
	}

	@Override
	public void onExpired(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onExpired [" + path + "]");
		isConnected = false;
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onExpired(path, type);
			}
		}
	}

	@Override
	public void onAuthFailed(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onAuthFailed [" + path + "]");
		isConnected = false;
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onAuthFailed(path, type);
			}
		}
	}

	@Override
	public void onConnectedReadOnly(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onConnectedReadOnly [" + path + "]");
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onConnectedReadOnly(path, type);
			}
		}
	}

	@Override
	public void onDisconnected(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onDisconnected [" + path + "]");
		isConnected = false;
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onDisconnected(path, type);
			}
		}
	}

	@Override
	public void onSaslAuthenticated(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onSaslAuthenticated [" + path + "]");
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onSaslAuthenticated(path, type);
			}
		}
	}

	@Override
	public void onUnknown(
		String path,
		EventType type
	) {
		log.info("ZOOKEEPER Event Invoked : onUnknown [" + path + "]");
		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onUnknown(path, type);
			}
		}
	}

	@Override
	public void onNodeChildChanged(String path) {

		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onNodeChildChanged(path);
			}
		}
	}

	@Override
	public void onNodeCreated(String path) {

		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onNodeCreated(path);
			}
		}
	}

	@Override
	public void onNodeDataChanged(String path) {

		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onNodeDataChanged(path);
			}
		}
	}

	@Override
	public void onNodeDeleted(String path) {

		for (ZookeeperWatcherEventHandler hh : eventHandlerList) {
			if (hh != null) {
				hh.onNodeDeleted(path);
			}
		}
	}
}
