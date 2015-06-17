package com.infogen.authc;

import com.infogen.authc.subject.Subject;

/**
 * API认证框架的工具类,可以获取subject
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class ThreadLocal_Auth {

	private static final ThreadLocal<Subject> thread_local_subject = new ThreadLocal<>();

	public static Subject getSubject() {
		Subject subject = thread_local_subject.get();
		return subject;
	}

	public static void setSubject(Subject subject) {
		thread_local_subject.set(subject);
	}

}
