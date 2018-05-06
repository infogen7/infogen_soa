/**
 * 
 */
package com.infogen.core.tools;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class IP {
	private static final Logger LOGGER = LogManager.getLogger(IP.class.getName());

	/**
	 * 
	 * @param string_ip
	 *            ip的string类型
	 * @return IP转成数字类型
	 */
	public static Long ip_to_long(String string_ip) {
		long[] ip = new long[4];
		int position1 = string_ip.indexOf(".");
		int position2 = string_ip.indexOf(".", position1 + 1);
		int position3 = string_ip.indexOf(".", position2 + 1);
		ip[0] = Long.parseLong(string_ip.substring(0, position1));
		ip[1] = Long.parseLong(string_ip.substring(position1 + 1, position2));
		ip[2] = Long.parseLong(string_ip.substring(position2 + 1, position3));
		ip[3] = Long.parseLong(string_ip.substring(position3 + 1));
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]; // ip1*256*256*256+ip2*256*256+ip3*256+ip4
	}

	/**
	 * @param request
	 *            HttpServletRequest
	 * @return 获取来源IP
	 */
	public static String get_remote_ip(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
			if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					LOGGER.error("找不到本地HOST", e);
				}
				ip = inet.getHostAddress();
			}
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = "0.0.0.0";
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ip.indexOf(",") > 0) {
			ip = ip.substring(0, ip.indexOf(","));
		}
		return ip;
	}

	/**
	 * @param nic_names
	 *            网卡名称的前缀，以","分隔 eg:eth,wlan
	 * @return 获取本机IP
	 */
	public static String get_local_ip(String... nic_names) {
		String ip = null;
		try {
			if (System.getProperty("os.name").indexOf("Linux") != -1) {
				for (String nic_name : nic_names) {
					if (nic_name == null) {
						continue;
					}
					ip = get_local_ip_bystartswith(nic_name);
					if (ip != null) {
						break;
					}
				}
			} else {
				ip = InetAddress.getLocalHost().getHostAddress().toString();
			}
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}

	private static String get_local_ip_bystartswith(String startsWith) throws SocketException {
		String ip = null;
		Enumeration<?> e1 = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
		while (e1.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) e1.nextElement();
			if (ni.getName().startsWith(startsWith)) {
				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = (InetAddress) e2.nextElement();
					if (ia instanceof Inet6Address) {
						continue;
					}
					ip = ia.getHostAddress();
				}
				break;
			}
		}
		return ip;
	}

	/**
	 * @return 获取本机主机名
	 */
	public static String get_hostname() {
		if (System.getenv("COMPUTERNAME") != null) {
			return System.getenv("COMPUTERNAME");
		} else {
			try {
				return (InetAddress.getLocalHost()).getHostName();
			} catch (UnknownHostException uhe) {
				String host = uhe.getMessage();
				if (host != null) {
					int colon = host.indexOf(':');
					if (colon > 0) {
						return host.substring(0, colon);
					}
				}
				return "UnknownHost";
			}
		}
	}

}
