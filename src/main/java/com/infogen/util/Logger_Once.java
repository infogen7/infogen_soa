package com.infogen.util;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 只会打印一次的日志
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午12:28:12
 * @since 1.0
 * @version 1.0
 */
public class Logger_Once {
	public static Logger logger = Logger.getLogger(Logger_Once.class.getName());
	private static Map<String, Boolean> map = new java.util.HashMap<>();

	private static Boolean has(String message) {
		if (map.get(message) == null) {
			map.put(message, true);
			return true;
		}
		return false;
	}

	public static void debug(String message) {
		if (has(message)) {
			return;
		}
		logger.debug(message);
	}

	public static void info(String message) {
		if (has(message)) {
			return;
		}
		logger.info(message);
	}

	public static void warn(String message) {
		if (has(message)) {
			return;
		}
		logger.warn(message);
	}

	public static void error(String message) {
		if (has(message)) {
			return;
		}
		logger.error(message);
	}

	public static void debug(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		logger.debug(message, e);
	}

	public static void info(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		logger.info(message, e);
	}

	public static void warn(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		logger.warn(message, e);
	}

	public static void error(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		logger.error(message, e);
	}
}
