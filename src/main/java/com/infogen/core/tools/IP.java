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

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class IP {
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
