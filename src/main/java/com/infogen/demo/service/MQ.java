package com.infogen.demo.service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infogen.aop.annotation.Execution_Logger;
import com.infogen.self_describing.annotation.Describe;
import com.infogen.self_describing.annotation.InParam;
import com.infogen.self_describing.annotation.OutParam;
import com.infogen.util.Return;

/**
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年11月12日 下午4:41:42
 */
@RestController
public class MQ {
	// ///////////////////////////////////request//////////////////////////////////////////////////////////////////
	@Execution_Logger
	@Describe(author = "larry", version = 1.0, value = "向队列发送消息", tags = "")
	@InParam(name = "token", describe = "任务令牌")
	@InParam(name = "message", describe = "消息")
	@RequestMapping(value = "send", method = RequestMethod.GET)
	public Return send(@RequestParam(required = true) String token, @RequestParam(required = true) String message) throws UnsupportedEncodingException {
		if (token.trim().isEmpty() || message.trim().isEmpty()) {
			return Return.FAIL(400, "参数不能为空");
		}
		try {
			return Return.SUCCESS(200, "提交成功");
		} catch (Exception e) {
			return Return.FAIL(500, "发送失败");
		}
	}

	/**
	 * 阻塞消息队列
	 * 
	 * @param userForMobile
	 * @return
	 */
	@Execution_Logger
	@Describe(author = "larry", version = 1.0, value = "从队列取得消息", tags = "")
	@InParam(name = "token", describe = "任务令牌")
	@OutParam(name = "message", type = String.class, describe = "取得的消息")
	@RequestMapping(value = "get", method = RequestMethod.GET)
	public Callable<Return> get(@RequestParam(required = true) String token) {
		return new Callable<Return>() {
			@Override
			public Return call() throws Exception {
				if (token.trim().isEmpty()) {
					return Return.FAIL(400, "参数不能为空");
				}
				try {
					Thread.sleep(3000);
					return Return.SUCCESS(200, "成功").put("message", "message");
				} catch (Exception e) {
					return Return.FAIL(500, "失败");
				}
			}
		};
	}

}
