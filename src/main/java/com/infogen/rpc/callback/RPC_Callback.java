/**
 * 
 */
package com.infogen.rpc.callback;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.thrift.Response;

/**
 *  rpc异步调用的返回值
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月15日 下午3:38:49
 */
public class RPC_Callback {
	public static final Logger logger = Logger.getLogger(RPC_Callback.class.getName());

	private ArrayBlockingQueue<Response> queue = new ArrayBlockingQueue<Response>(1);

	public Boolean add(Response value) {
		if (value == null) {
			return false;
		}
		return queue.add(value);
	}

	public Response get(Long seconds) {
		try {
			Response poll = queue.poll(seconds, TimeUnit.SECONDS);
			return poll;
		} catch (Exception e) {
			logger.error("获取异步返回值异常", e);
		}
		return null;
	}

}
