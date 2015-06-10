/**
 * 
 */
package com.infogen.server.model;

import java.time.Clock;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * 为本地调用扩展的服务属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月28日 上午10:03:46
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class NativeServer extends AbstractServer {
	public static final Logger logger = Logger.getLogger(NativeServer.class.getName());

	private CopyOnWriteArrayList<NativeNode> available_nodes = new CopyOnWriteArrayList<>();
	private CopyOnWriteArrayList<NativeNode> disabled_nodes = new CopyOnWriteArrayList<>();

	@JsonIgnore
	private transient ConcurrentHashMap<Integer, NativeNode> load_balanc_map = new ConcurrentHashMap<>();
	@JsonIgnore
	private transient String change_node_status_lock = "";

	// ////////////////////////////////////////// 定时修正不可用的节点/////////////////////////////////////////////////////
	public void add(NativeNode node) {
		if (node.available()) {
			available_nodes.add(node);
		} else {
			logger.error("node unavailable:".concat(Tool_Jackson.toJson(node)));
		}
	}

	private void recover() {
		synchronized (change_node_status_lock) {
			available_nodes.addAll(disabled_nodes);
			Integer count = load_balanc_map.size();
			for (NativeNode node : disabled_nodes) {
				for (int i = 0; i < node.getRatio(); i++) {
					load_balanc_map.put(count, node);
					count++;
				}
			}
			disabled_nodes.clear();
		}
	}

	public void rehash() {
		load_balanc_map.clear();
		Integer count = 0;
		for (NativeNode node : available_nodes) {
			for (int i = 0; i < node.getRatio(); i++) {
				load_balanc_map.put(count, node);
				count++;
			}
		}
	}

	public void disabled(NativeNode node) {
		synchronized (change_node_status_lock) {
			disabled_nodes.add(node);
			available_nodes.remove(node);
		}
		rehash();
	}

	/**
	 * 获取随机节点实现负载均衡
	 * 
	 * @return
	 */

	private long last_invoke_millis = Clock.system(InfoGen_Configuration.zoneid).millis();

	public NativeNode random_node() {
		long millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		// 没有可用节点或距离上一次调用超过指定时间
		if (available_nodes.size() == 0 || (millis - last_invoke_millis) > 500000) {
			if (disabled_nodes.size() > 0) {
				recover();
			}
		}
		last_invoke_millis = millis;

		NativeNode node = null;
		int size = load_balanc_map.size();
		if (size > 0) {
			node = load_balanc_map.get(new Random().nextInt(size));
		}
		return node;
	}

	// ///////////////////////////////////////////////////////////getter setter////////////////////////////////////////
	public CopyOnWriteArrayList<NativeNode> getAvailable_nodes() {
		return available_nodes;
	}

	public void setAvailable_nodes(CopyOnWriteArrayList<NativeNode> available_nodes) {
		this.available_nodes = available_nodes;
	}

	public CopyOnWriteArrayList<NativeNode> getDisabled_nodes() {
		return disabled_nodes;
	}

	public void setDisabled_nodes(CopyOnWriteArrayList<NativeNode> disabled_nodes) {
		this.disabled_nodes = disabled_nodes;
	}

}
