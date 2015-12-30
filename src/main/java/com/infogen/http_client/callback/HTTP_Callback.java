package com.infogen.http_client.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * http异步调用的返回值
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:51:06
 * @since 1.0
 * @version 1.0
 */
public class HTTP_Callback<T> {
	private static final Logger LOGGER = LogManager.getLogger(HTTP_Callback.class.getName());

	private ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(1);

	public T get(Integer seconds) {
		try {
			return queue.poll(seconds, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.error("获取异步返回值异常", e);
		}
		return null;
	}

	public void run(T value) {
		if (value == null) {
			LOGGER.warn("参数不能为空");
			return;
		}
		queue.add(value);
	}
}
