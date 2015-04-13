/**
 * 
 */
package com.infogen.http.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.util.Return;

/**
 * http异步调用的返回值
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月15日 下午3:38:49
 */
public class Http_Callback {
	public static final Logger logger = Logger.getLogger(Http_Callback.class.getName());

	private ArrayBlockingQueue<Return> queue = new ArrayBlockingQueue<Return>(1);

	public Boolean add(Return value) {
		if (value == null) {
			return false;
		}
		return queue.add(value);
	}

	public Return get(Long seconds) {
		try {
			Return poll = queue.poll(seconds, TimeUnit.SECONDS);
			return poll;
		} catch (Exception e) {
			logger.error("获取异步返回值异常", e);
		}
		return null;
	}

}
