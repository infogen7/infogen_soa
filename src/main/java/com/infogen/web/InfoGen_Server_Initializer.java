package com.infogen.web;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.locks.StampedLock;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.FilterRegistration.Dynamic;

import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.infogen.authc.web.InfoGen_Auth_Filter;
import com.infogen.web.ServletContainerInitializer.WebApplicationInitializer;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_Server_Initializer implements WebApplicationInitializer {
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
				// dispatcher.setLoadOnStartup(1);// 确保在default servlet加载完成之后再加载
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

			Dynamic addFilter = servletContext.addFilter("Default_Web_Auth_Filter", InfoGen_Auth_Filter.class);
			EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
			dispatcherTypes.add(DispatcherType.REQUEST);

			addFilter.addMappingForUrlPatterns(dispatcherTypes, true, "/*");
			addFilter.setAsyncSupported(true);
		} catch (IOException e) {
			e.printStackTrace();
			servletContext.log("启动MVC框架失败");
			System.exit(1);
		}
	}
}
