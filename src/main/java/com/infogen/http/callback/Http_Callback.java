/**
 * 
 */
package com.infogen.http.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * http异步调用的返回值
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月15日 下午3:38:49
 */
public class Http_Callback {
	private static final Logger logger = Logger.getLogger(Http_Callback.class.getName());

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
				return Return.FAIL(CODE._502);
			} else {
				return Return.create(poll);
			}
		} catch (Exception e) {
			logger.error("获取异步返回值异常", e);
			return Return.FAIL(CODE._510);
		}
	}
}
