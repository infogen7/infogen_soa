/**
 * 
 */
package com.infogen.http.listener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.locks.StampedLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.infogen.InfoGen;
import com.infogen.InfoGen_Kafka;
import com.infogen.InfoGen_Thrift;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.security.InfoGen_Security;
import com.infogen.self_describing.component.OutParameter;
import com.infogen.util.NativePath;

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
		// 更新web.xml中的白名单
		ServletRegistration servletRegistration = servletContext.getServletRegistration("default");
		Collection<String> white_lists = new ArrayList<>();

		String contextPath = servletContext.getContextPath();
		for (String mapping : servletRegistration.getMappings()) {
			if (!mapping.startsWith(contextPath)) {
				mapping = contextPath.concat(mapping);
			}
			white_lists.add(mapping);
		}
		InfoGen_Security.getInstance().add_ignore(white_lists);

		servletContext.log("启动MVC框架 ...");
		try {
			if (servletContext.getContextPath().equals("/infogen_soa")) {
				// 用于本机调试
				Properties service_properties = new Properties();
				try (InputStream resourceAsStream = Files.newInputStream(NativePath.get("infogen.properties"), StandardOpenOption.READ);//
						InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);) {
					service_properties.load(inputstreamreader);
				}
				InfoGen_Configuration config = new InfoGen_Configuration(service_properties);
				config.add_basic_outparameter(new OutParameter("note", String.class, false, "", "错误描述"));
				config.add_basic_outparameter(new OutParameter("code", Integer.class, true, "200", "错误码<br>200 成功<br>400 参数不正确<br>401 特定参数不符合条件(eg:没有这个用户)<br>404 没有这个方法 (RPC调用)<br>500 错误"));

				InfoGen.getInstance().start_and_watch(config).register();
				InfoGen_Kafka.getInstance().start(config);
				InfoGen_Thrift.getInstance().start_asyn(config);
			}
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
