package com.infogen.tracking;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.tools.Tool_Context;
import com.infogen.tracking.enum0.Track_Header;

/**
 * Track的工具类,可以获取存放在ThreadLocal中的对象
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_HTTP_Tracking_Handle {
	// 初始化配置时赋值
	public static RegisterServer register_server = InfoGen_Configuration.register_server;
	public static RegisterNode register_node = InfoGen_Configuration.register_node;

	public static String sessionid_name = "token";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.web.InfoGen_SOA_Filter_Handle#doFilter(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public CallChain doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		CallChain callchain = new CallChain();

		// traceid
		String traceid = request.getParameter(Track_Header.x_track_id.key);
		if (traceid != null) {
			callchain.setTrackid(traceid);
			// session id
			callchain.setSessionid(request.getHeader(Track_Header.x_session_id.key));
			// identify
			callchain.setIdentify(request.getHeader(Track_Header.x_identify.key));
			// sequence
			callchain.setSequence(Integer.valueOf(request.getHeader(Track_Header.x_sequence.key)) + 1);
			// Referer
			callchain.setReferer(request.getHeader(Track_Header.x_referer.key));
		} else {// 客户端调用
			callchain.setTrackid(UUID.randomUUID().toString().replaceAll("-", ""));
			// session id
			String sessionid = request.getParameter(sessionid_name);
			if (sessionid == null) {
				sessionid = Tool_Context.get_cookie(request, sessionid_name);
			}
			callchain.setSessionid(sessionid);
			// identify
			String identify = Tool_Context.get_cookie(request, Track_Header.x_identify.key);
			if (identify == null) {
				identify = UUID.randomUUID().toString().replaceAll("-", "");
				Tool_Context.set_cookie(response, Track_Header.x_identify.key, identify);
			}
			callchain.setIdentify(identify);
			// sequence
			callchain.setSequence(0);
			// Referer
			callchain.setReferer(request.getHeader("Referer"));
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
		ThreadLocal_Tracking.setCallchain(callchain);
		return callchain;
	}
}