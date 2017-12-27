package com.infogen.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
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

	public InfoGen_Jetty start(Integer http_port, String context) {
		return start(http_port, context, NativePath.get("webapp").toString(), NativePath.get("webapp/WEB-INF/web.xml").toString());
	}

	public InfoGen_Jetty start(Integer http_port, String context, String webapp_path) {
		return start(http_port, context, webapp_path, NativePath.get("webapp/WEB-INF/web.xml").toString());
	}

	public InfoGen_Jetty start(Integer http_port, String context, String webapp_path, String descriptor) {
		classpaths.add(NativePath.get_class_path(InfoGen_Jetty.class));
		Thread t = new Thread(() -> {
			try {
				final Server server = createServerInSource(http_port, context, webapp_path, descriptor, classpaths.toArray(new String[] {}));
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

	private ServerConnector getHttpConnector(Server server, int port) {
		HttpConfiguration config = new HttpConfiguration();
		config.setSecureScheme("https");
		config.setSecurePort(443);
		config.setRequestHeaderSize(52428800);
		List<Customizer> customizers = new ArrayList<>();
		customizers.add(new ForwardedRequestCustomizer());
		config.setCustomizers(customizers);
		ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(config));
		connector.setPort(port);
		return connector;
	}

	// 创建用于开发运行调试的Jetty Server, 以src/main/webapp为Web应用目录.
	public Server createServerInSource(int port, String context, String default_webapp_path, String descriptor, String[] classpaths) throws MalformedURLException {
		Server server = new Server();
		// 设置在JVM退出时关闭Jetty的钩子。
		server.setStopAtShutdown(true);

		ServerConnector httpConnector = getHttpConnector(server, port);
		server.setConnectors(new Connector[] { httpConnector });

		WebAppContext webContext = new WebAppContext();
		webContext.setContextPath(context);
		webContext.setResourceBase(default_webapp_path);
		webContext.setDescriptor(descriptor);
		webContext.setMaxFormContentSize(52428800);
		webContext.setParentLoaderPriority(true);

		// JSP 相关 指定temp目录会导致jsp编译后的class文件路径和包名不匹配
		// File temp = NativePath.get("temp").toFile();
		// webContext.setTempDirectory(temp);
		// webContext.setAttribute("javax.servlet.context.tempdir", temp);
		// // 下面是为解决此次问题增加的代码
		// webContext.setAttribute("org.eclipse.jetty.containerInitializers", Arrays.asList(new ContainerInitializer(new JettyJasperInitializer(), null)));
		// webContext.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
		// webContext.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*taglibs.*\\.jar$");

		// 配置jetty扫描注解的目录 并去重
		Set<Resource> set = new HashSet<>();
		try {
			set.add(Resource.newResource(NativePath.get_class_path()));
			set.add(Resource.newResource(NativePath.get_class_path(InfoGen_Jetty.class)));
			try {
				Class<?> clazz = this.getClass().getClassLoader().loadClass("com.infogen.authc.InfoGen_Session");
				set.add(Resource.newResource(NativePath.get_class_path(clazz)));
			} catch (ClassNotFoundException e) {
				LOGGER.warn("未加载认证模块 InfoGen_Authc");
			}
			if (classpaths != null) {
				for (String classpath : classpaths) {
					set.add(Resource.newResource(classpath));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
		webContext.setConfigurations(new Configuration[] { new AnnotationConfiguration(), new WebInfConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration(), new EnvConfiguration(), new PlusConfiguration(), new JettyWebXmlConfiguration(), new WebXmlConfiguration() });

		//
		HandlerCollection handlerCollection = new HandlerCollection();
		handlerCollection.setHandlers(new Handler[] { webContext });
		server.setHandler(handlerCollection);
		return server;
	}

	public static void main(String[] args) throws Exception {
		Thread.currentThread().join();
	}

}
