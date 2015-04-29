package com.infogen.http;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_Server_Listener implements WebApplicationInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.WebApplicationInitializer#onStartup(javax.servlet.ServletContext)
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		// 更新web.xml中的白名单
		// ServletRegistration servletRegistration = servletContext.getServletRegistration("default");
		// Collection<String> white_lists = new ArrayList<>();
		// String contextPath = servletContext.getContextPath();
		// for (String mapping : servletRegistration.getMappings()) {
		// if (!mapping.startsWith(contextPath)) {
		// mapping = contextPath.concat(mapping);
		// }
		// white_lists.add(mapping);
		// }
		// InfoGen_Security.getInstance().add_ignore(white_lists);
	}
}
