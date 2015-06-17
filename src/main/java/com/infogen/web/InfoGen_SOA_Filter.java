package com.infogen.web;

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

import com.infogen.authc.InfoGen_Authc_Handle;
import com.infogen.tracking.InfoGen_HTTP_Tracking_Handle;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
@WebFilter(filterName = "InfoGen_SOA_Filter", urlPatterns = { "/*" }, asyncSupported = true)
public class InfoGen_SOA_Filter implements Filter {
	private InfoGen_HTTP_Tracking_Handle track = new InfoGen_HTTP_Tracking_Handle();
	private InfoGen_Authc_Handle authc = new InfoGen_Authc_Handle();

	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;
		track.doFilter(request, response);

		if (!authc.doFilter(request, response)) {
			return;
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