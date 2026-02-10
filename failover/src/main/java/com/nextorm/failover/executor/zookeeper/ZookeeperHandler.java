package com.nextorm.failover.executor.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Zookeeper Library를 직접 참조하는 Class 입니다.
 * TIme Out 계산 법 :
 * connectTimeout = sessionTimeout / hostsList.length;
 * readTimeout = sessionTimeout * 2 / 3;
 *
 * @author Alex Lee
 */
@Slf4j
public class ZookeeperHandler {
	private final ZooKeeper zk;

	public ZookeeperHandler(ZooKeeper zk) {
		this.zk = zk;
	}

	/**
	 * 임시 노드를 생성합니다.
	 * 임시 노드는 휘발성 노드로, 프로그램이 종료되면 자동으로 제거되는 노드 입니다.
	 *
	 * @return 생성한 Node의 이름을 리턴합니다. 만일 생성하지 못하였을 때는 NONE나 null, "" 이 리턴 됩니다.
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	public String createNode(
		String nodeName,
		String userData,
		boolean isPersistNode,
		boolean isSequential,
		boolean isWatchEvent
	) throws KeeperException, InterruptedException {
		String retVal = "NONE";
		if (zk != null) {
			try {
				if (!isPersistNode) {    //임시 노드 생성
					if (isSequential) {
						retVal = zk.create(nodeName,
							userData.getBytes(Charset.forName("UTF-8")),
							ZooDefs.Ids.OPEN_ACL_UNSAFE,
							CreateMode.EPHEMERAL_SEQUENTIAL);
					} else {
						retVal = zk.create(nodeName,
							userData.getBytes(Charset.forName("UTF-8")),
							ZooDefs.Ids.OPEN_ACL_UNSAFE,
							CreateMode.EPHEMERAL);
					}
				} else {    //영구 노드 생성
					if (isSequential) {
						retVal = zk.create(nodeName,
							userData.getBytes(Charset.forName("UTF-8")),
							ZooDefs.Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT_SEQUENTIAL);
					} else {
						retVal = zk.create(nodeName,
							userData.getBytes(Charset.forName("UTF-8")),
							ZooDefs.Ids.OPEN_ACL_UNSAFE,
							CreateMode.PERSISTENT);
					}
				}
			} catch (NodeExistsException ex) {
				log.error("Node Already Exists : " + nodeName);
			}
		}
		return retVal;
	}

	/**
	 * 특정 노드를 삭제합니다.
	 *
	 * @param nodeName
	 * @param isInvokeEvent
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public void deleteNode(
		String nodeName,
		boolean isInvokeEvent
	) throws KeeperException, InterruptedException {
		if (isExistNode(nodeName, isInvokeEvent) && (zk != null)) {
			try {
				zk.delete(nodeName, -1);
			} catch (NoNodeException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 특정 Node가 존재하는지 여부를 확인 합니다.
	 *
	 * @param nodeName
	 * @param isWatchEvent
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public boolean isExistNode(
		String nodeName,
		boolean isWatchEvent
	) throws KeeperException, InterruptedException {
		boolean isExist = false;
		if (zk != null) {
			Stat stat = zk.exists(nodeName, isWatchEvent);
			if (stat != null) {
				isExist = true;
			}
		}

		return isExist;
	}

	public List<String> getChildren(
		String nodeName,
		boolean isWatchEvent
	) throws KeeperException, InterruptedException {
		List<String> childrenList = null;
		if (zk != null) {
			childrenList = zk.getChildren(nodeName, isWatchEvent);
		}
		return childrenList;
	}

	public List<String> getChildren(
		String nodeName,
		boolean isWatchEvent,
		Stat stat
	) throws KeeperException, InterruptedException {
		List<String> childrenList = null;
		if (zk != null) {
			childrenList = zk.getChildren(nodeName, isWatchEvent, stat);
		}
		return childrenList;
	}

	public List<String> getChildren(
		String nodeName,
		Watcher watcher
	) throws KeeperException, InterruptedException {
		List<String> childrenList = null;
		if (zk != null) {
			childrenList = zk.getChildren(nodeName, watcher);
		}
		return childrenList;
	}

	public List<String> getChildren(
		String nodeName,
		Watcher watcher,
		Stat stat
	) throws KeeperException, InterruptedException {
		List<String> childrenList = null;
		if (zk != null) {
			childrenList = zk.getChildren(nodeName, watcher);
		}
		return childrenList;
	}

	public void checkNodeAsync(
		String nodeName,
		Watcher watcher,
		StatCallback callback,
		Object ctx
	) {
		if (zk != null) {
			zk.exists(nodeName, watcher, callback, ctx);
		}
	}

	public void addWatcher(Watcher watcher) {
		if (zk != null) {
			zk.register(watcher);
		}
	}

	public String getNodeData(
		String nodeName,
		boolean isWatchEvent
	) throws KeeperException, InterruptedException {
		String retVal = "";
		if (zk != null) {
			Stat stat = new Stat();
			retVal = new String(zk.getData(nodeName, isWatchEvent, stat));
		}
		return retVal;
	}

	public String getNodeData(
		String nodeName,
		boolean isWatchEvent,
		Stat stat
	) throws KeeperException, InterruptedException {
		String retVal = "";
		if (zk != null) {
			retVal = new String(zk.getData(nodeName, isWatchEvent, stat));
		}
		return retVal;
	}

	/**
	 * 특정 노드의 값을 수정합니다.
	 *
	 * @param nodeName
	 * @param data
	 * @return 수정한 후, Version을 리턴 합니다. 다음 수정 시, 동일한 Version으로 수정을 요청해야 합니다.
	 */
	public int setNodeData(
		String nodeName,
		String data,
		int version
	) throws KeeperException, InterruptedException {
		int retVersion = 0;
		if (zk != null) {
			Stat stat = zk.setData(nodeName, data.getBytes(StandardCharsets.UTF_8), version);
			retVersion = stat.getVersion();
		}
		return retVersion;
	}

	public void stop() throws InterruptedException {
		if (zk != null) {
			zk.close();
			// zk = null;
		}
	}
}
