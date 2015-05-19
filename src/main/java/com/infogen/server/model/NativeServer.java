/**
 * 
 */
package com.infogen.server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.util.Scheduled;

/**
 * 为本地调用扩展的服务属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月28日 上午10:03:46
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class NativeServer extends AbstractServer {
	private List<NativeNode> available_nodes = new ArrayList<>();
	private List<NativeNode> disabled_nodes = new ArrayList<>();

	@JsonIgnore
	private transient ConcurrentHashMap<Integer, NativeNode> load_balanc_map = new ConcurrentHashMap<>();
	@JsonIgnore
	private transient String rehash_load_balanc_map_write_lock = "";

	public NativeServer() {
		// 定时修正不可用的节点 这个时间需要考虑zookeeper的超时时间
		Scheduled.executors.scheduleWithFixedDelay(() -> {
			if (!disabled_nodes.isEmpty()) {
				append(disabled_nodes);
			}
		}, 3, 3, TimeUnit.SECONDS);
	}

	// ////////////////////////////////////////// 定时修正不可用的节点/////////////////////////////////////////////////////

	public void append(List<NativeNode> nodes) {
		synchronized (rehash_load_balanc_map_write_lock) {
			Integer count = load_balanc_map.size();
			for (NativeNode node : nodes) {
				if (node.available()) {
					for (int i = 0; i < node.getRatio(); i++) {
						load_balanc_map.put(count, node);
						count++;
					}
				}
			}
		}
	}

	public void rehash() {
		synchronized (rehash_load_balanc_map_write_lock) {
			load_balanc_map.clear();
			Integer count = 0;
			for (NativeNode node : available_nodes) {
				if (node.available()) {
					for (int i = 0; i < node.getRatio(); i++) {
						load_balanc_map.put(count, node);
						count++;
					}
				}
			}
		}
	}

	public void disabled(NativeNode node) {
		disabled_nodes.add(node);
		available_nodes.remove(node);
		rehash();
	}

	/**
	 * 获取随机节点实现负载均衡
	 * 
	 * @return
	 */
	public NativeNode random_node() {
		NativeNode node = null;
		int size = load_balanc_map.size();
		if (size > 0) {
			node = load_balanc_map.get(new Random().nextInt(size));
		}
		return node;
	}

	public List<NativeNode> getAvailable_nodes() {
		return available_nodes;
	}

}
