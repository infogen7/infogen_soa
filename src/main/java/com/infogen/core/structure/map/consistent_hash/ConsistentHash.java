package com.infogen.core.structure.map.consistent_hash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.core.structure.map.consistent_hash.hash.HashFunction;

/**
 * 一致性hash
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 上午10:42:37
 * @since 1.0
 * @version 1.0
 */
public class ConsistentHash<T extends ShardInfo> {
	private static final Logger LOGGER = LogManager.getLogger(ConsistentHash.class.getName());
	public static final Charset charset = StandardCharsets.UTF_8;

	private HashFunction algo = HashFunction.MURMUR_HASH;
	private TreeMap<Long, T> nodes = new TreeMap<>();

	public ConsistentHash() {
	}

	public ConsistentHash(List<T> shards) {
		initialize(shards);
	}

	public ConsistentHash(List<T> shards, HashFunction algo) {
		this.algo = algo;
		initialize(shards);
	}

	// 默认的虚拟节点数目与 shardInfo.getWeight()一起使用
	private static final int basic_virtual_node_number = 16;

	private void initialize(List<T> shards) {
		for (int i = 0; i != shards.size(); ++i) {
			add(shards.get(i));
		}
	}

	public void add(T shardInfo) {
		for (int n = 0; n < basic_virtual_node_number * shardInfo.getRatio(); n++) {
			try {
				nodes.put(this.algo.hash(new StringBuilder(shardInfo.getName()).append("*").append(shardInfo.getRatio()).append("*").append(n).toString(), charset), shardInfo);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("添加节点失败", e);
			}
		}
	}

	public void remove(T shardInfo) {
		for (int n = 0; n < basic_virtual_node_number * shardInfo.getRatio(); n++) {
			try {
				nodes.remove(this.algo.hash(new StringBuilder(shardInfo.getName()).append("*").append(shardInfo.getRatio()).append("*").append(n).toString(), charset));
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("删除节点失败", e);
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////get////////////////////////////////////////////////////////////////////////

	public T get(String seed) {
		Entry<Long, T> entry = nodes.ceilingEntry(algo.hash(seed.getBytes(charset)));
		if (entry == null) {
			entry = nodes.firstEntry();
		}
		if (entry == null) {
			return null;
		}
		return entry.getValue();
	}

	public Collection<ShardInfo> getAllShardInfo() {
		return Collections.unmodifiableCollection(nodes.values());
	}
}
