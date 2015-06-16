package com.infogen.util.map.consistent_hash;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 上午11:35:59
 * @since 1.0
 * @version 1.0
 */
public abstract class ShardInfo {
	private int weight;

	public ShardInfo() {
	}

	public ShardInfo(int weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return this.weight;
	}

	public abstract String getName();
}
