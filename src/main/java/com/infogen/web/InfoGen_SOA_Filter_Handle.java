package com.infogen.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
public abstract class InfoGen_SOA_Filter_Handle {
	public abstract Boolean doFilter(HttpServletRequest srequset, HttpServletResponse sresponse) throws IOException, ServletException;
}