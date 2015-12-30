package com.infogen.http.mvc_framework;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.infogen.http.ServletContainerInitializer.WebApplicationInitializer;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:51:20
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Spring implements WebApplicationInitializer {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Spring.class.getName());
	private static ServletContext servletContext;
	private static String mapping_path;
	private static String mapping_pattern;
	private final static byte[] lock = new byte[0];

	public static void config_mvc(String mapping_path, String mapping_pattern) throws IOException {
		InfoGen_Spring.mapping_path = mapping_path;
		InfoGen_Spring.mapping_pattern = mapping_pattern == null ? "/*" : mapping_pattern;
		run(servletContext);
	}

	public static void start_mvc(ServletContext servletContext) throws IOException {
		InfoGen_Spring.servletContext = servletContext;
		run(servletContext);
	}

	private static void run(ServletContext servletContext) throws IOException {
		synchronized (lock) {
			if (servletContext != null && mapping_path != null && !mapping_path.trim().isEmpty()) {
				XmlWebApplicationContext mvcContext = new XmlWebApplicationContext();
				mvcContext.setConfigLocation(mapping_path);

				DispatcherServlet dispatcherServlet = new DispatcherServlet(mvcContext);
				ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", dispatcherServlet);
				dispatcher.setAsyncSupported(true);// 支持异步servlet
				dispatcher.setLoadOnStartup(1);// 确保在default servlet加载完成之后再加载
				dispatcher.addMapping(mapping_pattern);
			}
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
