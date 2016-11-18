package com.infogen.core.json;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP协议调用端输入参数类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月12日 下午3:56:17
 * @since 1.0
 * @version 1.0
 */
public class Parameters extends IdentityHashMap<String, String> {
	private static final long serialVersionUID = -5436768657673377874L;

	public static Parameters create() {
		return new Parameters();
	}

	public static Parameters create(Map<String, List<String>> name_value_pair) {
		Parameters identityhashmap = new Parameters();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				identityhashmap.put(new String(key), value);
			});
		});
		return identityhashmap;
	}

	public static Parameters create(String key, String value) {
		return new Parameters().add(key, value);
	}

	public Parameters add(String key, String value) {
		super.put(key, value);
		return this;
	}

	@Override
	public String put(String key, String value) {
		super.put(key, value);
		return value;
	}
}
