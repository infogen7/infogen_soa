package com.infogen.authc;

import org.apache.log4j.Logger;

import com.infogen.InfoGen;
import com.infogen.authc.subject.Subject;

/**
 * API认证框架的session本地缓存工具类,可以保存和获取subject
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Auth {
	private static final Logger LOGGER = Logger.getLogger(InfoGen.class.getName());
	private static final ThreadLocal<Subject> thread_local_subject = new ThreadLocal<>();

	public static Subject getSubject() {
		Subject subject = thread_local_subject.get();
		if (subject == null) {
			subject = new Subject();
			LOGGER.warn("没有找到当前线程存储的subject,检查是否有存入,或当前代码是否是在新创建的线程里执行的");
		}
		return subject;
	}

	public static void setSubject(Subject subject) {
		thread_local_subject.set(subject);
	}

}
