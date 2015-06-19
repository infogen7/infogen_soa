package com.infogen.http.tracking;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.tools.Tool_Context;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.tracking.enum0.Track;

/**
 * Track的工具类,可以获取存放在ThreadLocal中的对象
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_HTTP_Tracking_Handle {
	// 初始化配置时赋值
	public static RegisterServer register_server = InfoGen_Configuration.register_server;;
	public static RegisterNode register_node = InfoGen_Configuration.register_node;;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.web.InfoGen_SOA_Filter_Handle#doFilter(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public CallChain doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		CallChain callchain = new CallChain();

		// traceid
		String traceid = request.getParameter(Track.x_track_id.key);
		if (traceid == null) {// 客户端调用
			callchain.setTrackid(UUID.randomUUID().toString().replaceAll("-", ""));
			// identify
			String identify = null;
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equalsIgnoreCase(Track.x_identify.key)) {
						identify = cookie.getValue();
					}
				}
			}
			if (identify == null) {
				identify = UUID.randomUUID().toString().replaceAll("-", "");
				int day = 24 * 60 * 60;
				Cookie cookie = new Cookie(Track.x_identify.key, UUID.randomUUID().toString().replaceAll("-", ""));
				cookie.setMaxAge(7 * day);
				cookie.setPath("/");
				cookie.setHttpOnly(true);
				response.addCookie(cookie);
			}
			callchain.setIdentify(identify);
			// sequence
			callchain.setSequence(0);
			// Referer
			callchain.setReferer(request.getHeader("Referer"));
		} else {
			callchain.setTrackid(traceid);
			// identify
			callchain.setIdentify(request.getParameter(Track.x_identify.key));
			// sequence
			callchain.setSequence(Integer.valueOf(request.getParameter(Track.x_sequence.key)) + 1);
			// Referer
			callchain.setReferer(request.getParameter(Track.x_referer.key));
		}

		// referer ip
		callchain.setReferer_ip(Tool_Context.get_ip(request));
		// target
		String target = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (target.startsWith(contextPath)) {
			target = target.substring(contextPath.length());
		}
		callchain.setTarget(target);
		// target ip
		callchain.setTarget_ip(register_node.getIp());
		// target server 当前服务
		callchain.setTarget_server(register_server.getName());

		//
		ThreadLocal_Tracking.setRequest(request);
		ThreadLocal_Tracking.setResponse(response);
		ThreadLocal_Tracking.setCallchain(callchain);
		return callchain;
	}
}