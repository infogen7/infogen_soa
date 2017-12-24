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
public class Parameter extends IdentityHashMap<String, Object> {
	private static final long serialVersionUID = -5436768657673377874L;

	public static Parameter create() {
		return new Parameter();
	}

	public static Parameter create(Map<String, List<String>> name_value_pair) {
		Parameter map = new Parameter();
		name_value_pair.forEach((key, values) -> {
			values.forEach(value -> {
				map.put(new String(key), value);
			});
		});
		return map;
	}

	public static Parameter create(String key, Object value) {
		return new Parameter().put(key, value);
	}

	public Parameter put(String key, Object value) {
		super.put(key, value);
		return this;
	}
}
