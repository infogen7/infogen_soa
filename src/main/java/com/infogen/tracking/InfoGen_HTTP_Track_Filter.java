package com.infogen.tracking;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.http.Tool_HTTP;

/**
 * HTTP方式的调用链日志框架的过滤器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
@WebFilter(filterName = "InfoGen_HTTP_Track_Filter", urlPatterns = { "/*" }, asyncSupported = true)
public class InfoGen_HTTP_Track_Filter implements Filter {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_HTTP_Track_Filter.class.getName());

	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;
		try {
			CallChain callchain = new CallChain();
			// traceid
			String traceid = request.getParameter(HTTP_Header.x_track_id.key);
			if (traceid == null || traceid.isEmpty()) {
				traceid = UUID.randomUUID().toString().replaceAll("-", "");
			}
			callchain.setTrackid(traceid);
			// identify
			callchain.setIdentify(request.getHeader(HTTP_Header.x_identify.key));
			// session id
			callchain.setSessionid(request.getHeader(HTTP_Header.x_session_id.key));
			// sequence
			String x_sequence = request.getHeader(HTTP_Header.x_sequence.key);
			Integer sequence = x_sequence == null ? 0 : Integer.valueOf(x_sequence);
			callchain.setSequence(sequence + 1);
			// referer ip
			callchain.setReferer_ip(Tool_HTTP.get_ip(request));
			// target
			String target = request.getRequestURI();
			String contextPath = request.getContextPath();
			target = target.startsWith(contextPath) ? target.substring(contextPath.length()) : target;
			callchain.setTarget(target);
			// Referer
			String x_referer = request.getHeader(HTTP_Header.x_referer.key);
			x_referer = x_referer == null ? request.getHeader("Referer") : x_referer;
			callchain.setReferer(x_referer);

			//
			ThreadLocal_Tracking.setCallchain(callchain);
		} catch (Exception e) {
			LOGGER.error("调用链日志初始化失败", e);
		}
		filterChain.doFilter(request, response);
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