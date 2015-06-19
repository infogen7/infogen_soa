package com.infogen.http.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * 基本工具方法
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 * @version 创建时间 2013-4-1 下午4:16:43
 */
public class Tool_Context {
	private static final Logger LOGGER = Logger.getLogger(Tool_Context.class.getName());

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
