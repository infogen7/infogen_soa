package com.infogen.http.ServletContainerInitializer;

import java.io.IOException;
import java.util.concurrent.locks.StampedLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.log4j.Logger;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_Server_Initializer implements WebApplicationInitializer {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Server_Initializer.class.getName());
	public static ServletContext servletContext;
	public static String config_path;
	public static String mapping;
	private static final StampedLock lock = new StampedLock();

	public static void start_mvc(String config_path, String mapping) throws IOException {
		InfoGen_Server_Initializer.config_path = config_path;
		InfoGen_Server_Initializer.mapping = mapping == null ? "/*" : mapping;
		start_mvc(servletContext, config_path);
	}

	public static void start_mvc(ServletContext servletContext) throws IOException {
		InfoGen_Server_Initializer.servletContext = servletContext;
		start_mvc(servletContext, config_path);
	}

	private static void start_mvc(ServletContext servletContext, String config_path) throws IOException {
		long stamp = lock.writeLock();
		try {
			if (servletContext != null && config_path != null && !config_path.trim().isEmpty()) {
				XmlWebApplicationContext mvcContext = new XmlWebApplicationContext();
				mvcContext.setConfigLocation(config_path);

				DispatcherServlet dispatcherServlet = new DispatcherServlet(mvcContext);
				ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
				dispatcher.setAsyncSupported(true);// 支持异步servlet
				dispatcher.setLoadOnStartup(1);// 确保在default servlet加载完成之后再加载
				dispatcher.addMapping(mapping);
			}
		} finally {
			lock.unlockWrite(stamp);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.WebApplicationInitializer#onStartup(javax.servlet.ServletContext)
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		try {
			start_mvc(servletContext);
		} catch (IOException e) {
			LOGGER.error("启动MVC框架失败:", e);
			System.exit(1);
		}
	}
}
