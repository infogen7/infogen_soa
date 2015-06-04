package com.infogen.track;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.infogen.web.InfoGen_SOA_Filter_Handle;

/**
 * Track的工具类,可以获取存放在ThreadLocal中的对象
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Track_Handle extends InfoGen_SOA_Filter_Handle {
	public final Logger logger = Logger.getLogger(InfoGen_Track_Handle.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.web.InfoGen_SOA_Filter_Handle#doFilter(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		InfoGen_Track.setRequest(request);
		InfoGen_Track.setResponse(response);
		// TODO
		// cookie等用户标识,客户端类型 ,traceid,sequence,来源地址 ,来源ip,当前地址,当前ip,当前服务,调用时间 ,调用时长,调用状态 ,数据大小,sessionid(token)
		// a00000... ,测试/京东/聚信立 ,tr00000,0 ,home.html ,xx ,send ,xx ,中控 ,2015050X ,300ms ,ok/error/auth,1.3k ,t0000
		return true;
	}
}
