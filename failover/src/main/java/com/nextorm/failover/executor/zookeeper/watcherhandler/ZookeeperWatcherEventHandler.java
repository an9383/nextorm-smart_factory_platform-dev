package com.nextorm.failover.executor.zookeeper.watcherhandler;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

public interface ZookeeperWatcherEventHandler extends Watcher {
	void onSyncConnected(
		String path,
		EventType type
	);

	void onExpired(
		String path,
		EventType type
	);

	void onAuthFailed(
		String path,
		EventType type
	);

	void onConnectedReadOnly(
		String path,
		EventType type
	);

	void onDisconnected(
		String path,
		EventType type
	);

	void onSaslAuthenticated(
		String path,
		EventType type
	);

	void onUnknown(
		String path,
		EventType type
	);

	void onNodeChildChanged(String path);

	void onNodeCreated(String path);

	void onNodeDataChanged(String path);

	void onNodeDeleted(String path);
}
