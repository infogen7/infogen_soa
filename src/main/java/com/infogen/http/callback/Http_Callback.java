package com.infogen.http.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * http异步调用的返回值
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:00:48
 * @since 1.0
 * @version 1.0
 */
public class Http_Callback {
	private static final Logger LOGGER = Logger.getLogger(Http_Callback.class.getName());

	private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(1);

	public Boolean add(String value) {
		if (value == null) {
			return false;
		}
		return queue.add(value);
	}

	public Return get(Long seconds) {
		try {
			String poll = queue.poll(seconds, TimeUnit.SECONDS);
			if (poll == null) {
				return Return.FAIL(CODE.timeout);
			} else {
				return Return.create(poll);
			}
		} catch (Exception e) {
			LOGGER.error("获取异步返回值异常", e);
			return Return.FAIL(CODE.generate_return_error);
		}
	}
}
