package com.infogen.http.ServletContainerInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * 服务启动时调用该接口的实现类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月30日 下午4:22:05
 * @since 1.0
 * @version 1.0
 */
public interface WebApplicationInitializer {
	public void onStartup(ServletContext servletContext) throws ServletException;

}
