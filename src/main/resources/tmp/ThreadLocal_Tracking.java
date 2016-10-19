package com.infogen.tracking;

/**
 * AOP的工具类,可以获取存放在ThreadLocal中的调用链对象
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class ThreadLocal_Tracking {
	private static final ThreadLocal<CallChain> callChain = new ThreadLocal<>();

	public static ThreadLocal<CallChain> getCallchain() {
		return callChain;
	}

	public static void setCallchain(CallChain callchain0) {
		callChain.set(callchain0);
	}
}
