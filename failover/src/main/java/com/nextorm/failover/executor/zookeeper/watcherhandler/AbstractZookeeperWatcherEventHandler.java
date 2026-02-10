package com.nextorm.failover.executor.zookeeper.watcherhandler;

import org.apache.zookeeper.WatchedEvent;

public abstract class AbstractZookeeperWatcherEventHandler implements ZookeeperWatcherEventHandler {
	@Override
	public void process(WatchedEvent event) {
		invokeTypeEvent(event);
	}

	private void invokeTypeEvent(WatchedEvent event) {
		switch (event.getType()) {
			case NodeChildrenChanged:

				try {
					this.onNodeChildChanged(event.getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case NodeCreated:

				try {
					this.onNodeCreated(event.getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case NodeDataChanged:

				try {
					this.onNodeDataChanged(event.getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case NodeDeleted:

				try {
					this.onNodeDeleted(event.getPath());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case None:
				invokeStatusEvent(event);
				break;
		}

	}

	private void invokeStatusEvent(WatchedEvent event) {
		switch (event.getState()) {
			case AuthFailed:

				try {
					this.onAuthFailed(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case ConnectedReadOnly:

				try {
					this.onConnectedReadOnly(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case Disconnected:

				try {
					this.onDisconnected(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case Expired:

				try {
					this.onExpired(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case SaslAuthenticated:

				try {
					this.onSaslAuthenticated(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case SyncConnected:

				try {
					this.onSyncConnected(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			default:

				try {
					this.onUnknown(event.getPath(), event.getType());
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
		}
	}

}
