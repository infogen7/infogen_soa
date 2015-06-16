package com.infogen.util.map.consistent_hash;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.Service;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.util.map.consistent_hash.hash.HashFunction;

/**
 * 一致性hash
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 上午10:42:37
 * @since 1.0
 * @version 1.0
 */
public class ConsistentHash {
	public static final Logger logger = Logger.getLogger(Service.class.getName());
	public static final int DEFAULT_WEIGHT = 1;
	public static final Pattern DEFAULT_KEY_TAG_PATTERN = Pattern.compile("\\{(.+?)\\}");// 获取 {} 中间的内容

	private HashFunction algo = HashFunction.MURMUR_HASH;
	private TreeMap<Long, ShardInfo> nodes;
	private Map<String, ShardInfo> resources = new LinkedHashMap<>();

	public ConsistentHash(List<ShardInfo> shards) {
		initialize(shards);
	}

	public ConsistentHash(List<ShardInfo> shards, HashFunction algo) {
		this.algo = algo;
		initialize(shards);
	}

	// 默认的虚拟节点数目与 shardInfo.getWeight()一起使用
	private final int basic_virtual_node_number = 16;

	private void initialize(List<ShardInfo> shards) {
		nodes = new TreeMap<Long, ShardInfo>();
		for (int i = 0; i != shards.size(); ++i) {
			add(shards.get(i));
		}
	}

	public void add(ShardInfo shardInfo) {
		for (int n = 0; n < basic_virtual_node_number * shardInfo.getWeight(); n++) {
			try {
				nodes.put(this.algo.hash(new StringBuilder(shardInfo.getName()).append("*").append(shardInfo.getWeight()).append("*").append(n).toString(), InfoGen_Configuration.charset), shardInfo);
			} catch (UnsupportedEncodingException e) {
				logger.error("添加节点失败", e);
			}
		}
		resources.put(shardInfo.getName(), shardInfo);
	}

	public void remove(ShardInfo shardInfo) {
		for (int n = 0; n < basic_virtual_node_number * shardInfo.getWeight(); n++) {
			try {
				nodes.remove(this.algo.hash(new StringBuilder(shardInfo.getName()).append("*").append(shardInfo.getWeight()).append("*").append(n).toString(), InfoGen_Configuration.charset));
			} catch (UnsupportedEncodingException e) {
				logger.error("删除节点失败", e);
			}
		}
		resources.remove(shardInfo.getName());
	}

	// ////////////////////////////////////////////////////////////////////////get////////////////////////////////////////////////////////////////////////

	public ShardInfo getShardInfo(String key) {
		try {
			SortedMap<Long, ShardInfo> tail = nodes.tailMap(algo.hash(key.getBytes("UTF-8")));
			if (tail.isEmpty()) {
				return nodes.get(nodes.firstKey());
			}
			return tail.get(tail.firstKey());
		} catch (UnsupportedEncodingException e) {
			logger.error("获取节点描述失败", e);
		}
		return null;
	}

	public Collection<ShardInfo> getAllShardInfo() {
		return Collections.unmodifiableCollection(nodes.values());
	}

	public Collection<ShardInfo> getAllShards() {
		return Collections.unmodifiableCollection(resources.values());
	}
}
