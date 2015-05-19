package com.infogen.backups.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
@Deprecated
public class InfoGen_Security_Interceptor implements HandlerInterceptor {
	public static Logger logger = Logger.getLogger(InfoGen_Security_Interceptor.class.getName());
	// 权限配置
	public InfoGen_Security infogen_security = InfoGen_Security.getInstance();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 当前请求页面
		String ip = Tool_Context.get_ip(request);

		if (infogen_security.authentication(request)) {
			return true;
		} else {
			// 必须否则前台会收到乱码
			response.setContentType("text/html;charset=".concat(InfoGen_Configuration.charset.name()));
			// 不在任何规则中
			response.getWriter().write(Return.FAIL(CODE._501.code, CODE._501.note).put("ip", ip).toJson());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		// TODO Auto-generated method stub

	}

}