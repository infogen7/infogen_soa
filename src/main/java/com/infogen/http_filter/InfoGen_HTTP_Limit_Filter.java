package com.infogen.http_filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

import com.infogen.limit.configuration.handle.impl.Limit_Properties_Handle_HTTP_Limit;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * HTTP方式的接口并发数限流的过滤器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
@WebFilter(filterName = "InfoGen_HTTP_Filter_3", urlPatterns = { "/*" }, asyncSupported = true)
public class InfoGen_HTTP_Limit_Filter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_HTTP_Limit_Filter.class.getName());
	private static final Map<String, AtomicInteger> map = new HashMap<>();
	// 初始化配置时赋值
	public static final Map<String, Integer> limits = Limit_Properties_Handle_HTTP_Limit.map;

	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;

		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (requestURI.startsWith(contextPath)) {
			requestURI = requestURI.substring(contextPath.length());
		}

		Integer limit = limits.get(requestURI);
		if (limit == null) {
			filterChain.doFilter(srequset, sresponse);
			return;
		}

		// 需要判断限制
		AtomicInteger integer = map.get(requestURI);
		if (integer == null) {
			integer = new AtomicInteger(0);
			map.put(requestURI, integer);
		}
		if (integer.get() > limit) {
			LOGGER.info("接口调用超过限制:".concat(requestURI).concat("-").concat(limit.toString()));
			response.getWriter().write(Return.FAIL(CODE.limit).toJson());
			return;
		} else {
			try {
				integer.incrementAndGet();
				filterChain.doFilter(srequset, sresponse);
			} finally {
				integer.decrementAndGet();
			}
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