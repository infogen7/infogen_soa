package com.infogen.core.structure.map.consistent_hash;

import java.io.Serializable;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 上午11:35:59
 * @since 1.0
 * @version 1.0
 */
public abstract class ShardInfo implements Serializable {
	private static final long serialVersionUID = 383882568446801529L;
	protected String name;// 节点名称 注意:不能重复!!! 用来hash后作为一致性hash的值
	protected Integer ratio = 10;// 1-10的整数 一致性哈希实现负载均衡的权重

	public Integer getRatio() {
		return ratio;
	}

	public void setRatio(Integer ratio) {
		this.ratio = ratio;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
