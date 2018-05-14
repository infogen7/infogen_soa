package com.infogen.rpc_client.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.RpcCallback;

/**
 * rpc异步调用的返回值
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:18:34
 * @since 1.0
 * @version 1.0
 * @param <T>
 *            指定类型
 */
public class InfoGen_Callback<T> implements RpcCallback<T> {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Callback.class.getName());

	private ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(1);

	public T get(Integer seconds) {
		try {
			return queue.poll(seconds, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOGGER.error("获取异步返回值异常", e);
		}
		return null;
	}

	@Override
	public void run(T value) {
		if (value == null) {
			LOGGER.warn("参数不能为空");
		} else {
			queue.add(value);
		}
	}
}
