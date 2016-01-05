package com.infogen.http;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.infogen.core.util.NativePath;

/**
 * 启动jetty服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:07:55
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Jetty {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Jetty.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Jetty instance = new InfoGen_Jetty();
	}

	public static InfoGen_Jetty getInstance() {
		return InnerInstance.instance;
	}

	private List<String> classpaths = new ArrayList<>();

	public InfoGen_Jetty add_classpath(Class<?> clazz) {
		classpaths.add(NativePath.get_class_path(clazz));
		return this;
	}

	// 启动jetty服务
	public InfoGen_Jetty start(Integer http_port) {
		return start(http_port, "/", NativePath.get("webapp").toString(), NativePath.get("webapp/WEB-INF/web.xml").toString());
	}

	public InfoGen_Jetty start(Integer http_port, String CONTEXT) {
		return start(http_port, CONTEXT, NativePath.get("webapp").toString(), NativePath.get("webapp/WEB-INF/web.xml").toString());
	}

	public InfoGen_Jetty start(Integer http_port, String CONTEXT, String DEFAULT_WEBAPP_PATH, String DESCRIPTOR) {
		classpaths.add(NativePath.get_class_path(InfoGen_Jetty.class));
		Thread t = new Thread(() -> {
			try {
				final Server server = createServerInSource(http_port, CONTEXT, DEFAULT_WEBAPP_PATH, DESCRIPTOR, classpaths.toArray(new String[] {}));
				server.start();
				server.join();
			} catch (Exception e) {
				LOGGER.error("启动jetty失败", e);
				System.exit(-1);
			}
		});
		t.setDaemon(true);
		t.start();
		return this;
	}

	// 创建用于开发运行调试的Jetty Server, 以src/main/webapp为Web应用目录.
	public Server createServerInSource(int port, String context, String default_webapp_path, String descriptor, String[] classpaths) throws MalformedURLException {
		Server server = new Server();
		// 设置在JVM退出时关闭Jetty的钩子。
		server.setStopAtShutdown(true);

		// 这是http的连接器
		// Common HTTP configuration.
		HttpConfiguration config = new HttpConfiguration();
		// HTTP/1.1 support.
		HttpConnectionFactory http1_1 = new HttpConnectionFactory(config);
		// HTTP/2 cleartext support.
		// HTTP2CServerConnectionFactory http2 = new
		// HTTP2CServerConnectionFactory(config);
		ServerConnector connector = new ServerConnector(server, http1_1);
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
		Set<Resource> set = new HashSet<>();
		set.add(Resource.newResource(NativePath.get_class_path()));
		set.add(Resource.newResource(NativePath.get_class_path(InfoGen_Jetty.class)));
		if (classpaths != null) {
			for (String classpath : classpaths) {
				set.add(Resource.newResource(classpath));
			}
		}
		// file:///home/juxinli/workspace/infogen_soa/target/classes/
		for (Resource resource : set) {
			LOGGER.info("add jetty annotation config dir => " + resource.getName());
			webContext.getMetaData().addContainerResource(resource);
		}
		// JettyWebConfiguration. Looks for Xmlconfiguration files in WEB-INF.
		// Searches in order for the first of jetty6-web.xml, jetty-web.xml or
		// web-jetty.xml
		// WebXmlConfiguration Configure by parsing default web.xml and web.xml
		// AnnotationConfiguration eg:@WebFilter
		// 配置jetty支持xml和注解配置
		webContext.setConfigurations(new Configuration[] { new JettyWebXmlConfiguration(), new WebXmlConfiguration(), new AnnotationConfiguration() });
		// webContext.setConfigurations(new Configuration[] { new
		// WebXmlConfiguration() });
		// ClassList cl = Configuration.ClassList.setServerDefault(server);
		// cl.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
		// "org.eclipse.jetty.annotations.AnnotationConfiguration");

		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.setHandlers(new Handler[] { webContext });
		server.setHandler(handlerCollection);

		return server;
	}

	public static void main(String[] args) throws Exception {
		Thread.currentThread().join();
	}

}
