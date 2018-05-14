package com.infogen;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * infogen内部定时调度器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:36:25
 * @since 1.0
 * @version 1.0
 */
public class Scheduled {
	/**
	 * infogen 定时对加载服务和监听纠错 调用程序不要使用
	 */
	public static ScheduledExecutorService executors_single = Executors.newSingleThreadScheduledExecutor((r) -> {
		Thread thread = Executors.defaultThreadFactory().newThread(r);
		thread.setDaemon(true);
		return thread;
	});
}
