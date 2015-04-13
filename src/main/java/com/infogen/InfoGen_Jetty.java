package com.infogen;

/**
 * 启动jetty服务
 * @author larry
 * @email   larrylv@outlook.com
 * @version 创建时间 2014年10月22日 下午6:55:23
 */
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.util.NativePath;

public class InfoGen_Jetty {
	public final Logger logger = Logger.getLogger(InfoGen_Jetty.class.getName());

	private static class InnerInstance {
		public static InfoGen_Jetty instance = new InfoGen_Jetty();
	}

	public static InfoGen_Jetty getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Jetty() {
	}

	/**
	 * 启动jetty服务
	 * 
	 * @param infogen_configuration
	 * @param CONTEXT
	 * @param DEFAULT_WEBAPP_PATH
	 * @param DESCRIPTOR
	 * @return
	 */
	public InfoGen_Jetty start(InfoGen_Configuration infogen_configuration, String CONTEXT, String DEFAULT_WEBAPP_PATH, String DESCRIPTOR) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final Server server = createServerInSource(infogen_configuration.http_port, CONTEXT, DEFAULT_WEBAPP_PATH, DESCRIPTOR);
					server.stop();
					server.start();
					server.join();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
		t.setDaemon(true);
		t.start();
		return this;
	}

	/**
	 * 创建用于开发运行调试的Jetty Server, 以src/main/webapp为Web应用目录.
	 * 
	 * @throws MalformedURLException
	 */
	private Server createServerInSource(int port, String context, String default_webapp_path, String descriptor) throws MalformedURLException {

		Server server = new Server();
		// 设置在JVM退出时关闭Jetty的钩子。
		server.setStopAtShutdown(true);

		// 这是http的连接器
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		// 解决Windows下重复启动Jetty居然不报告端口冲突的问题. 但是可能会造成linux上产生僵尸进程
		// connector.setReuseAddress(false);
		server.setConnectors(new Connector[] { connector });

		WebAppContext webContext = new WebAppContext();
		webContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		webContext.setContextPath(context);
		webContext.setResourceBase(default_webapp_path);
		webContext.setDescriptor(descriptor);
		// 配置jetty扫描注解的目录 并去重
		logger.info("add jetty annotation config dir => " + Resource.newResource(NativePath.get_class_path()));
		logger.info("add jetty annotation config dir => " + Resource.newResource(NativePath.get_infogen_class_path()));
		Set<Resource> set = new HashSet<>();
		set.add(Resource.newResource(NativePath.get_class_path()));
		set.add(Resource.newResource(NativePath.get_infogen_class_path()));
		for (Resource resource : set) {
			webContext.getMetaData().addContainerResource(resource);
		}
		// JettyWebConfiguration. Looks for Xmlconfiguration files in WEB-INF. Searches in order for the first of jetty6-web.xml, jetty-web.xml or web-jetty.xml
		// WebXmlConfiguration Configure by parsing default web.xml and web.xml
		// AnnotationConfiguration eg:@WebFilter
		// 配置jetty支持xml和注解配置
		webContext.setConfigurations(new Configuration[] { new JettyWebXmlConfiguration(), new WebXmlConfiguration(), new AnnotationConfiguration() });
		// webContext.setConfigurations(new Configuration[] { new WebXmlConfiguration() });
		// ClassList cl = Configuration.ClassList.setServerDefault(server);
		// cl.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.setHandlers(new Handler[] { webContext });
		server.setHandler(handlerCollection);

		return server;
	}

	public static void main(String[] args) throws Exception {

		Thread.currentThread().join();
	}

}
