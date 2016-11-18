/**
 * 
 */
package com.infogen.core.tools;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Tool_Throwable {
	/**
	 * @param e
	 *            异常类
	 * @return 拼接打印 exception 栈内容
	 */
	public static String stacktrace(Throwable e) {
		StringBuilder stack_trace = new StringBuilder();
		while (e != null) {
			String error_message = e.getMessage();
			error_message = error_message == null ? "\r\n" : error_message.concat("\r\n");
			stack_trace.append(error_message);
			stack_trace.append("<br>");
			for (StackTraceElement string : e.getStackTrace()) {
				stack_trace.append(string.toString());
				stack_trace.append("<br>");
			}
			e = e.getCause();
		}
		return stack_trace.toString();
	}
}
