package com.infogen.rpc.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.protobuf.RpcCallback;

/**
 * rpc异步调用的返回值
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:18:34
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_RPCCallback implements RpcCallback<Object> {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_RPCCallback.class.getName());

	private ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);

	public Object get(Integer milliseconds) {
		try {
			return queue.poll(milliseconds, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LOGGER.error("获取异步返回值异常", e);
		}
		return null;
	}

	@Override
	public void run(Object value) {
		queue.add(value);
	}

}
