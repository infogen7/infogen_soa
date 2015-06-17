package com.infogen.tracking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AOP的工具类,可以获取存放在ThreadLocal中的对象
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class ThreadLocal_Tracking {
	private static final ThreadLocal<HttpServletRequest> request = new ThreadLocal<>();
	private static final ThreadLocal<HttpServletResponse> response = new ThreadLocal<>();
	private static final ThreadLocal<CallChain> callChain = new ThreadLocal<>();

	public static HttpServletRequest getRequest() {
		return request.get();
	}

	public static void setRequest(HttpServletRequest request0) {
		request.set(request0);
	}

	public static HttpServletResponse getResponse() {
		return response.get();
	}

	public static void setResponse(HttpServletResponse response0) {
		response.set(response0);
	}

	public static ThreadLocal<CallChain> getCallchain() {
		return callChain;
	}

	public static void setCallchain(CallChain callchain0) {
		callChain.set(callchain0);
	}
}
