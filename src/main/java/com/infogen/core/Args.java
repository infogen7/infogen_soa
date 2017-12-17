package com.infogen.core;

/**
 * 判断参数的空值情况
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:34:23
 * @since 1.0
 * @version 1.0
 */
public class Args {
	public static Boolean isEmpty(Object object) {
		if (object == null || object.toString().trim().isEmpty()) {
			return true;
		}
		return false;
	}

	public static Boolean hasNull(Object... objects) {
		for (Object object : objects) {
			if (object == null) {
				return true;
			}
		}
		return false;
	}

	public static Boolean hasEmpty(Object... objects) {
		for (Object object : objects) {
			if (object == null || object.toString().trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public static Boolean allEmpty(Object... objects) {
		for (Object object : objects) {
			if (object != null && !object.toString().trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}
}
