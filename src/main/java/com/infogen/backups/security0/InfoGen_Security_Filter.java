package com.infogen.backups.security0;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.tools.Tool_Context;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * http白名单过滤的过滤器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
//@WebFilter(filterName = "InfoGen_Security_Filter", urlPatterns = { "/*" }, asyncSupported = true)
public class InfoGen_Security_Filter implements Filter {
	public static Logger logger = Logger.getLogger(InfoGen_Security_Filter.class.getName());

	// 权限配置
	public InfoGen_Security infogen_security = InfoGen_Security.getInstance();

	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;
		String ip = Tool_Context.get_ip(request);

		if (infogen_security.authentication(request)) {
			filterChain.doFilter(request, response);
		} else {
			// 必须否则前台会收到乱码
			response.setContentType("text/html;charset=".concat(InfoGen_Configuration.charset.name()));
			// 不在任何规则中
			response.getWriter().write(Return.FAIL(CODE._501.code, CODE._501.note).put("ip", ip).toJson());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
}