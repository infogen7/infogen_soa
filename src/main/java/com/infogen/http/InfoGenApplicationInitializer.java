package com.infogen.http;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.http.ServletContainerInitializer.WebApplicationInitializer;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:51:20
 * @since 1.0
 * @version 1.0
 */
public class InfoGenApplicationInitializer implements WebApplicationInitializer {
	private static final Logger LOGGER = LogManager.getLogger(InfoGenApplicationInitializer.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.WebApplicationInitializer#onStartup(javax.servlet
	 * .ServletContext)
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		LOGGER.info("initialized for Application");
		// XmlWebApplicationContext mvcContext = new XmlWebApplicationContext();
		// mvcContext.setConfigLocation(mapping_path);
		//
		// DispatcherServlet dispatcherServlet = new
		// DispatcherServlet(mvcContext);
		// ServletRegistration.Dynamic dispatcher =
		// servletContext.addServlet("dispatcher", dispatcherServlet);
		// dispatcher.setAsyncSupported(true);// 支持异步servlet
		// dispatcher.setLoadOnStartup(1);// 确保在default servlet加载完成之后再加载
		// dispatcher.addMapping(mapping_pattern);
		// created = true;
	}
}
