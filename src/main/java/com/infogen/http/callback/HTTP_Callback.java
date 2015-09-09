package com.infogen.http.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * http异步调用的返回值
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:18:34
 * @since 1.0
 * @version 1.0
 * @param <T>
 */
public class HTTP_Callback<T> {
	private static final Logger LOGGER = Logger.getLogger(HTTP_Callback.class.getName());

	private ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(1);

	public T get(Integer milliseconds) {
		try {
			return queue.poll(milliseconds, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LOGGER.error("获取异步返回值异常", e);
		}
		return null;
	}

	public void run(T value) {
		queue.add(value);
	}
}
