package com.infogen.backups.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.infogen.backups.security.component.Security;
import com.infogen.backups.security.component.WhiteList;
import com.infogen.tools.Tool_Context;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月8日 下午4:54:10
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Security {
	public final Logger logger = Logger.getLogger(InfoGen_Security.class.getName());

	private static class InnerInstance {
		public static InfoGen_Security instance = new InfoGen_Security();
	}

	public static InfoGen_Security getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Security() {
	}

	public Boolean authentication(HttpServletRequest request) {
		// request.getRealPath("/")); F:\Tomcat 6.0\webapps\news\test
		// System.out.println(request.getRequestURL()); // http://localhost:8080/news/main/list.jsp
		// System.out.println(request.getContextPath()); // /news
		// System.out.println(request.getServletPath()); // /main/list.jsp 配置spring mvc 的<mvc:default-servlet-handler />后始终为空
		// System.out.println(request.getRequestURI()); // /news/main/list.jsp
		// 当前请求页面
		String target_path = request.getRequestURI();
		String ip = Tool_Context.get_ip(request);
		return check_white_list(target_path, ip);
	}

	private Boolean check_white_list(String url, String ip) {
		// 例外
		if (security_ignore_equal.contains(url)) {
			return true;
		}
		for (String key : security_ignore_rule) {
			if (url.startsWith(key) || key.isEmpty()) {
				return true;
			}
		}
		// 规则白名单
		Set<String> ip_rules = security_white_list_equal.get(url);
		if (ip_rules != null) {
			for (String ip_pattern : ip_rules) {
				if (ip.startsWith(ip_pattern) || ip_pattern.isEmpty()) {
					return true;
				}
			}
		}
		for (Entry<String, Set<String>> entry : security_white_list_rule.entrySet()) {
			String key = entry.getKey();
			if (url.startsWith(key) || key.isEmpty()) {
				for (String ip_pattern : entry.getValue()) {
					if (ip.startsWith(ip_pattern) || ip_pattern.isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// url匹配 ip全部放行
	private static final Set<String> security_ignore_equal = new HashSet<>();
	private static final Set<String> security_ignore_rule = new HashSet<>();

	public void add_ignore(Collection<String> white_lists) {
		for (String url : white_lists) {
			if (url.endsWith("*")) {
				url = url.substring(0, url.length() - 1);
				security_ignore_rule.add(url);
			} else {
				security_ignore_equal.add(url);
			}
			if (url.contains("*")) {
				logger.error("白名单url格式错误 eg:/a/b/c  或 /a/b*:".concat(url));
			}
		}
	}

	// url匹配 ip匹配放行
	private static final Map<String, Set<String>> security_white_list_equal = new LinkedHashMap<>();
	private static final Map<String, Set<String>> security_white_list_rule = new LinkedHashMap<>();

	public void refresh_security(Security security) {
		security_white_list_equal.clear();
		security_white_list_rule.clear();
		List<WhiteList> white_lists = security.getWhite_lists();
		for (WhiteList whitelist : white_lists) {
			String url = whitelist.getUrl();
			String ip = whitelist.getIp();
			if (url == null || ip == null) {
				logger.error("白名单ip url 为空");
				continue;
			}
			if (ip.endsWith("*")) {
				ip = ip.substring(0, ip.length() - 1);
			}
			if (ip.contains("*")) {
				logger.error("白名单ip格式错误 eg:8.8.8.8  或 8.8*:".concat(ip));
				continue;
			}

			if (url.endsWith("*")) {
				url = url.substring(0, url.length() - 1);
				Set<String> orDefault = security_white_list_rule.getOrDefault(url, new HashSet<String>());
				orDefault.add(ip);
				security_white_list_rule.put(url, orDefault);
			} else {
				Set<String> orDefault = security_white_list_equal.getOrDefault(url, new HashSet<String>());
				orDefault.add(ip);
				security_white_list_equal.put(url, orDefault);
			}
			if (url.contains("*")) {
				logger.error("白名单url格式错误 eg:/a/b/c  或 /a/b*:".concat(url));
			}
		}
	}

}
