package com.infogen.cluster_limit;

import java.util.HashMap;
import java.util.Map;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年7月9日 下午4:04:30
 * @since 1.0
 * @version 1.0
 */
public class Limit_Model {
	public Map<String, Long> limits = new HashMap<>();// key:分组值 value:当前分组的限制
	private String group;// 分组名称
	private Integer timeslice = 1;// 滑动窗口时长

	protected Limit_Model() {
		super();
	}

	public Limit_Model(String group, Integer timeslice) {
		super();
		this.group = group;
		this.timeslice = timeslice;
	}

	public static Limit_Model create(String group, Integer timeslice) {
		return new Limit_Model(group, timeslice);
	}

	public Limit_Model put(String key, Long limit) {
		limits.put(key, limit);
		return this;
	}

	public Map<String, Long> getLimits() {
		return limits;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Integer getTimeslice() {
		return timeslice;
	}

	public void setTimeslice(Integer timeslice) {
		this.timeslice = timeslice;
	}

	public void setLimits(Map<String, Long> limits) {
		this.limits = limits;
	}

}
