package com.infogen.server.model;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.structure.map.consistent_hash.ConsistentHash;

/**
 * 为本地调用扩展的服务属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:30:02
 * @since 1.0
 * @version 1.0
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RemoteServer extends RegisterServer {
	@JsonIgnore
	private static final long serialVersionUID = 1343826022706203233L;

	@JsonIgnore
	private static final Logger LOGGER = LogManager.getLogger(RemoteServer.class.getName());

	private List<RemoteNode> available_nodes = new CopyOnWriteArrayList<>();
	private List<RemoteNode> disabled_nodes = new CopyOnWriteArrayList<>();

	private RemoteServer() {
	}

	@JsonIgnore
	private final transient ConsistentHash<RemoteNode> consistent_hash = new ConsistentHash<>();
	@JsonIgnore
	private final transient byte[] change_node_status_lock = new byte[0];
	@JsonIgnore
	private transient long last_success_invoke_millis = Clock.system(InfoGen_Configuration.zoneid).millis();// 最近一次重置失效节点的时间戳
	@JsonIgnore
	private final transient int disabled_timeout = 16 * 1000;// 重置失效节点的时间间隔 - 超过zookeeper的session超时时间
	@JsonIgnore
	private final transient int min_disabled_timeout = 3 * 1000;// 最小的节点恢复使用的间隔时间

	// ////////////////////////////////////////// 定时修正不可用的节点/////////////////////////////////////////////////////
	public Map<String, RemoteNode> get_all_nodes() {
		Map<String, RemoteNode> all_nodes = new HashMap<>();
		synchronized (change_node_status_lock) {
			available_nodes.forEach((node) -> {
				all_nodes.put(node.getName(), node);
			});
			disabled_nodes.forEach((node) -> {
				all_nodes.put(node.getName(), node);
			});
		}
		return all_nodes;
	}

	public void add(RemoteNode node) {
		if (node.available()) {
			synchronized (change_node_status_lock) {
				available_nodes.add(node);
				consistent_hash.add(node);
			}
		} else {
			LOGGER.error("node unavailable:".concat(node.getName()));
		}
	}

	public void remove(RemoteNode node) {
		synchronized (change_node_status_lock) {
			available_nodes.remove(node);
			disabled_nodes.remove(node);
			consistent_hash.remove(node);
		}
	}

	public void enable(RemoteNode node) {
		synchronized (change_node_status_lock) {
			disabled_nodes.remove(node);
			available_nodes.add(node);
			consistent_hash.add(node);
		}
	}

	public void disabled(RemoteNode node) {
		node.disabled_time = Clock.system(InfoGen_Configuration.zoneid).millis();
		synchronized (change_node_status_lock) {
			disabled_nodes.add(node);
			available_nodes.remove(node);
			consistent_hash.remove(node);
		}
	}

	// 获取随机节点实现负载均衡
	public RemoteNode random_node(String seed) {
		long millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		// 没有可用节点或距离重置失效节点的时间超过指定时间
		if (available_nodes.isEmpty() || (millis - last_success_invoke_millis) > disabled_timeout) {
			for (RemoteNode node : disabled_nodes) {
				if ((Clock.system(InfoGen_Configuration.zoneid).millis() - node.disabled_time) > min_disabled_timeout) {
					enable(node);
				}
			}
			last_success_invoke_millis = millis;
		}
		return consistent_hash.get(seed);
	}

	// ///////////////////////////////////////////////////////////getter setter////////////////////////////////////////
	public List<RemoteNode> getAvailable_nodes() {
		return available_nodes;
	}

	public void setAvailable_nodes(List<RemoteNode> available_nodes) {
		this.available_nodes = available_nodes;
	}

	public List<RemoteNode> getDisabled_nodes() {
		return disabled_nodes;
	}

	public void setDisabled_nodes(List<RemoteNode> disabled_nodes) {
		this.disabled_nodes = disabled_nodes;
	}

}
