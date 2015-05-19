/**
 * 
 */
package com.infogen.http;

import java.io.IOException;
import java.util.concurrent.locks.StampedLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
@WebListener
public class InfoGen_MVC_Listener implements ServletContextListener {
	public static ServletContext servletContext;
	public static String config_path;
	public static String mapping;
	private static final StampedLock lock = new StampedLock();

	public static void start_mvc(String config_path, String mapping) throws IOException {
		InfoGen_MVC_Listener.config_path = config_path;
		InfoGen_MVC_Listener.mapping = mapping == null ? "/*" : mapping;
		start_mvc(servletContext, config_path);
	}

	public static void start_mvc(ServletContext servletContext) throws IOException {
		InfoGen_MVC_Listener.servletContext = servletContext;
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
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		servletContext.log("启动MVC框架 ...");

		try {
			InfoGen_MVC_Listener.start_mvc(servletContext);
		} catch (Exception e) {
			e.printStackTrace();
			servletContext.log("启动MVC框架失败");
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}
}
