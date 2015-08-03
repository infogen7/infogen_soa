/**
 * 
 */
package com.infogen.rpc.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * rpc异步调用的返回值
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:18:34
 * @since 1.0
 * @version 1.0
 */
public class RPC_Callback<T> {
	private static final Logger LOGGER = Logger.getLogger(RPC_Callback.class.getName());

	private ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(1);

	public Boolean add(T value) {
		if (value == null) {
			return false;
		}
		return queue.add(value);
	}

	public T get(Long seconds) {
		try {
			return queue.poll(seconds, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.error("获取异步返回值异常", e);
		}
		return null;
	}

}
