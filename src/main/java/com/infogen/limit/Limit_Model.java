package com.infogen.limit;

import java.util.HashMap;
import java.util.Map;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年7月9日 下午4:04:30
 * @since 1.0
 * @version 1.0
 */
public class Limit_Model {
	public Map<String, Long> limits = new HashMap<>();
	private String group;
	private Long default_limit = Long.MAX_VALUE;

	public Map<String, Long> getLimits() {
		return limits;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Long getDefault_limit() {
		return default_limit;
	}

	public void setDefault_limit(Long default_limit) {
		this.default_limit = default_limit;
	}

}
