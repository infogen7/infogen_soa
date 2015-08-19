package com.infogen.http_filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.infogen.cluster_limit.InfoGen_HTTP_Cluster_Limit_Handle;

/**
 * HTTP方式的滑动时间片限流的过滤器
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
@WebFilter(filterName = "InfoGen_HTTP_Filter_2", urlPatterns = { "/*" }, asyncSupported = true)
public class InfoGen_HTTP_Cluster_Limit_Filter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_HTTP_Cluster_Limit_Filter.class.getName());

	private InfoGen_HTTP_Cluster_Limit_Handle cluster_limit = new InfoGen_HTTP_Cluster_Limit_Handle();

	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;

		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (requestURI.startsWith(contextPath)) {
			requestURI = requestURI.substring(contextPath.length());
		}
		try {
			if (!cluster_limit.doFilter(requestURI, request, response)) {
				return;
			}
		} catch (Exception e) {
			LOGGER.error("按分组限制调用次数失败", e);
		}

		filterChain.doFilter(srequset, sresponse);
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