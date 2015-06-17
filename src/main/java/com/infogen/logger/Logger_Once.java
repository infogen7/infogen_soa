package com.infogen.logger;

import org.apache.log4j.Logger;

import com.infogen.util.map.LRULinkedHashMap;

/**
 * 只会打印一次的日志
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午12:28:12
 * @since 1.0
 * @version 1.0
 */
public class Logger_Once {
	private static Logger LOGGER = Logger.getLogger(Logger_Once.class.getName());
	private static LRULinkedHashMap<String, Boolean> map = new LRULinkedHashMap<>(10000);

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
		LOGGER.debug(message);
	}

	public static void info(String message) {
		if (has(message)) {
			return;
		}
		LOGGER.info(message);
	}

	public static void warn(String message) {
		if (has(message)) {
			return;
		}
		LOGGER.warn(message);
	}

	public static void error(String message) {
		if (has(message)) {
			return;
		}
		LOGGER.error(message);
	}

	public static void debug(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		LOGGER.debug(message, e);
	}

	public static void info(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		LOGGER.info(message, e);
	}

	public static void warn(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		LOGGER.warn(message, e);
	}

	public static void error(String message, Throwable e) {
		if (has(message)) {
			return;
		}
		LOGGER.error(message, e);
	}
}
