package com.infogen.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.util.NativePath;

/**
 * 启动jetty服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:07:55
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Jetty extends InfoGen_Server {
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

	/**
	 * 启动jetty服务
	 * 
	 * @param infogen_configuration
	 * @param CONTEXT
	 * @param DEFAULT_WEBAPP_PATH
	 * @param DESCRIPTOR
	 * @return
	 */
	public InfoGen_Jetty start(InfoGen_Configuration infogen_configuration, String CONTEXT) {
		return start(infogen_configuration, CONTEXT, NativePath.get("webapp").toString(), NativePath.get("webapp/WEB-INF/web.xml").toString());
	}

	public InfoGen_Jetty start(InfoGen_Configuration infogen_configuration, String CONTEXT, String DEFAULT_WEBAPP_PATH, String DESCRIPTOR) {
		classpaths.add(NativePath.get_class_path(InfoGen_Jetty.class));
		Thread t = new Thread(() -> {
			try {
				final Server server = createServerInSource(infogen_configuration.register_node.getHttp_port(), CONTEXT, DEFAULT_WEBAPP_PATH, DESCRIPTOR, classpaths.toArray(new String[] {}));
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

	public static void main(String[] args) throws Exception {
		Thread.currentThread().join();
	}

}
