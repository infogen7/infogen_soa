package com.infogen.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * HTTP上下文相关的基本工具方法
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:31:18
 * @since 1.0
 * @version 1.0
 */
public class Tool_HTTP {
	private static final Logger LOGGER = Logger.getLogger(Tool_HTTP.class.getName());

	public static String get_cookie(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase(key)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static void set_cookie(HttpServletResponse response, String key, String value) {
		int day = 24 * 60 * 60;
		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge(7 * day);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	/**
	 * 获取 web 客户端IP
	 * 
	 * @param request
	 * @return
	 */
	public static String get_ip(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
				// 根据网卡取本机配置的IP
				try {
					ipAddress = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e) {
					LOGGER.warn("找不到主机名:", e);
				}
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.indexOf(",") > 0) { // "***.***.***.***".length() = 15
			ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
		}

		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			return null;
		}
		return ipAddress;
	}

}
