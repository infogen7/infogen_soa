/**
 * 
 */
package com.infogen.tools;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;

/**
 * 基本工具方法
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 * @version 创建时间 2013-4-1 下午4:16:43
 */
public class Tool_Core {
	public static Logger logger = Logger.getLogger(Tool_Core.class.getName());

	/**
	 * 拼接打印 exception 栈内容
	 * 
	 * @param e
	 * @return
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

	/**
	 * 创建文件并自动补全文件路径的缺失文件夹
	 * 
	 * @param paths
	 */
	public static void prepare_files(Path... paths) {
		for (Path path : paths) {
			try {
				if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
					Files.createFile(path);
				}
			} catch (IOException e) {
				logger.error("创建依赖文件夹失败", e);
			}
		}
	}

	/**
	 * 加载文件为字符串
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String load_file(Path path) throws IOException {
		// 获取缓存的服务配置
		StringBuffer sbf = new StringBuffer();
		Files.lines(path, InfoGen_Configuration.charset).forEach(line -> {
			sbf.append(line);
		});
		return sbf.toString();
	}

	/**
	 * MD5
	 * 
	 * @param password
	 * @param salt
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String MD5(String password, String salt) throws NoSuchAlgorithmException {
		MessageDigest instance = MessageDigest.getInstance("MD5");
		instance.update((password + "{" + salt.toString() + "}").getBytes(Charset.forName("UTF-8")));
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		byte tmp[] = instance.digest(); // MD5 的计算结果是一个 128 位的长整数，用字节表示就是 16 个字节
		char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，所以表示成 16 进制需要 32 个字符
		int k = 0; // 表示转换结果中对应的字符位置
		for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节转换成 16 进制字符的转换
			byte byte0 = tmp[i]; // 取第 i 个字节
			str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,>>> 为逻辑右移，将符号位一起右移
			str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
		}
		return new String(str);
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
					e.printStackTrace();
				}
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null) { // "***.***.***.***".length() = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}

		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			return null;
		}
		return ipAddress;
	}

	/**
	 * 获取本机IP
	 * 
	 * @return
	 */
	public static String getLocalIP() {
		String ip = null;
		try {
			if (System.getProperty("os.name").indexOf("Linux") != -1) {
				ip = get_local_ip_bystartsWith("eth");
				if (ip == null) {
					ip = get_local_ip_bystartsWith("wlan");
				}
			} else {
				ip = InetAddress.getLocalHost().getHostAddress().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ip;
	}

	private static String get_local_ip_bystartsWith(String startsWith) throws SocketException {
		String ip = null;
		Enumeration<?> e1 = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
		while (e1.hasMoreElements()) {
			NetworkInterface ni = (NetworkInterface) e1.nextElement();
			if (ni.getName().startsWith(startsWith)) {
				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = (InetAddress) e2.nextElement();
					if (ia instanceof Inet6Address)
						continue;
					ip = ia.getHostAddress();
				}
				break;
			}
		}
		return ip;
	}

	/**
	 * 获取本机主机名
	 * 
	 * @return
	 */
	public static String getHostName() {
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

	/**
	 * @trim
	 * @替换中文空格的特殊字符为普通空格
	 * @去掉字符串中前后的空格，并将中间多个连续的空格合并成一个
	 * @去掉开头出现的空格
	 * @param string
	 * @return
	 */

	public static String trim(String string) {
		if (string == null) {
			return null;
		}
		// 去掉特殊空格
		string = string.replace(" ", " ");
		// 并将中间多个连续的空格合并成一个
		String trim = Pattern.compile("[' ']+").matcher(string).replaceAll(" ").trim();
		//
		if (trim.startsWith(" ")) {
			trim = trim.substring(1);
		}
		return trim;
	}

	/**
	 * IP转成数字类型
	 * 
	 * @param string_ip
	 * @return
	 */
	@Deprecated
	public long ip_to_long(String string_ip) {
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

	private static final String PATTERN_LINE_START = "^";
	private static final String PATTERN_LINE_END = "$";
	private static final char[] META_CHARACTERS = { '$', '^', '[', ']', '(', ')', '{', '}', '|', '+', '.', '\\' };

	/**
	 * 通配符匹配
	 * 
	 * @param pattern
	 * @param str
	 * @return
	 */
	@Deprecated
	public static boolean pattern_match(String pattern, String str) {
		pattern = convertToRegexPattern(pattern);
		return Pattern.matches(pattern, str);
	}

	private static String convertToRegexPattern(String wildcardString) {
		String result = PATTERN_LINE_START;
		char[] chars = wildcardString.toCharArray();
		for (char ch : chars) {
			if (Arrays.binarySearch(META_CHARACTERS, ch) >= 0) {
				result += "\\" + ch;
				continue;
			}
			switch (ch) {
			case '*':
				result += ".*";
				break;
			case '?':
				result += ".{0,1}";
				break;
			default:
				result += ch;
			}
		}
		result += PATTERN_LINE_END;
		return result;
	}

}
