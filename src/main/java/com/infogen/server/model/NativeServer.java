package com.infogen.server.model;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.larrylgq.aop.tools.Tool_Jackson;
import com.larrylgq.aop.util.map.consistent_hash.ConsistentHash;

/**
 * 为本地调用扩展的服务属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月28日 上午10:03:46
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class NativeServer extends AbstractServer {
	private static final Logger LOGGER = Logger.getLogger(NativeServer.class.getName());

	private List<NativeNode> available_nodes = new CopyOnWriteArrayList<>();
	private List<NativeNode> disabled_nodes = new CopyOnWriteArrayList<>();

	private NativeServer() {
	}

	@JsonIgnore
	private transient ConsistentHash<NativeNode> consistent_hash = new ConsistentHash<>();
	@JsonIgnore
	private transient String change_node_status_lock = "";
	@JsonIgnore
	private long last_success_invoke_millis = Clock.system(InfoGen_Configuration.zoneid).millis();// 最近一次调用成功的时间戳
	@JsonIgnore
	private int disabled_timeout = 16 * 1000;// 重置失效节点的时间间隔 - 超过zookeeper的session超时时间
	@JsonIgnore
	private int min_disabled_timeout = 5 * 1000;// 最小的节点恢复使用的间隔时间

	// ////////////////////////////////////////// 定时修正不可用的节点/////////////////////////////////////////////////////
	public void add(NativeNode node) {
		if (node.available()) {
			available_nodes.add(node);
			consistent_hash.add(node);
		} else {
			LOGGER.error("node unavailable:".concat(Tool_Jackson.toJson(node)));
		}
	}

	public Map<String, NativeNode> get_all_nodes() {
		Map<String, NativeNode> all_nodes = new HashMap<>();
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

	public void remove(NativeNode node) {
		synchronized (change_node_status_lock) {
			available_nodes.remove(node);
			disabled_nodes.remove(node);
		}
		consistent_hash.remove(node);
	}

	private void recover() {
		for (NativeNode node : disabled_nodes) {
			if ((Clock.system(InfoGen_Configuration.zoneid).millis() - node.disabled_time) > min_disabled_timeout) {
				consistent_hash.add(node);
			}
		}
		synchronized (change_node_status_lock) {
			available_nodes.addAll(disabled_nodes);
			disabled_nodes.clear();
		}
	}

	public void disabled(NativeNode node) {
		consistent_hash.remove(node);
		node.disabled_time = Clock.system(InfoGen_Configuration.zoneid).millis();
		synchronized (change_node_status_lock) {
			disabled_nodes.add(node);
			available_nodes.remove(node);
		}
	}

	/**
	 * 获取随机节点实现负载均衡
	 * 
	 * @return
	 */

	public NativeNode random_node(String seed) {
		long millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		// 没有可用节点或距离上一次成功调用超过指定时间
		if (!disabled_nodes.isEmpty() && (available_nodes.isEmpty() || (millis - last_success_invoke_millis) > disabled_timeout)) {
			recover();
			last_success_invoke_millis = millis;
		}
		return consistent_hash.get(seed);
	}

	// ///////////////////////////////////////////////////////////getter setter////////////////////////////////////////
	public List<NativeNode> getAvailable_nodes() {
		return available_nodes;
	}

	public void setAvailable_nodes(List<NativeNode> available_nodes) {
		this.available_nodes = available_nodes;
	}

	public List<NativeNode> getDisabled_nodes() {
		return disabled_nodes;
	}

	public void setDisabled_nodes(List<NativeNode> disabled_nodes) {
		this.disabled_nodes = disabled_nodes;
	}

}
