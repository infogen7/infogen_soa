package com.infogen.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.http.InfoGen_MVC_Listener;
import com.infogen.logger.InfoGen_Logger_Kafka;
import com.infogen.logger.InfoGen_Logger;
import com.infogen.self_describing.InfoGen_Self_Describing;
import com.infogen.self_describing.component.Function;
import com.infogen.self_describing.component.OutParameter;
import com.infogen.server.RegisterNode;
import com.infogen.server.RegisterServer;
import com.infogen.tools.Tool_Core;
import com.infogen.util.NativePath;
import com.infogen.zookeeper.InfoGen_ZooKeeper;

/**
 * infogen配置解析及其它全局配置
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 * @version 创建时间 2013-9-30 下午1:57:58
 */

public class InfoGen_Configuration {
	public final static Logger logger = Logger.getLogger(InfoGen_Configuration.class.getName());
	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;
	public final static String infogen_logger_topic_execution_time = "infogen_logger_topic_execution_time";
	public final static String infogen_logger_topic_execution_exception = "infogen_logger_topic_execution_exception";
	public final static String infogen_logger_topic_invoke_time = "infogen_logger_topic_invoke_time";
	public final static String infogen_logger_topic_invoke_exception = "infogen_logger_topic_invoke_exception";

	// ////////////////////////////////////////////读取自身配置/////////////////////////////////////////////
	public static InfoGen_Logger infogen_logger = InfoGen_Logger_Kafka.getInstance();

	public RegisterNode register_node = new RegisterNode();
	public RegisterServer register_server = new RegisterServer();

	public String zookeeper;
	public String kafka;

	public Integer http_port;
	public Integer rpc_port;

	public String infogen_security_name;

	public InfoGen_Configuration(String infogen_path) throws IOException, URISyntaxException {
		Properties infogen_properties = new Properties();
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get("conf/infogen.properties"), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);) {
			infogen_properties.load(inputstreamreader);
		}
		initialization(infogen_properties);
	}

	public InfoGen_Configuration(Properties infogen_properties) throws IOException, URISyntaxException {
		initialization(infogen_properties);
	}

	public void add_basic_outparameter(OutParameter basic_outparameter) {
		for (Function function : register_server.getFunctions().values()) {
			function.getOut_parameters().add(basic_outparameter);
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////
	private void initialization(Properties infogen_properties) throws IOException, URISyntaxException {
		// zookeeper
		zookeeper = infogen_properties.getProperty("infogen.zookeeper");
		if (zookeeper == null || zookeeper.trim().isEmpty()) {
			logger.error("zookeeper配置不能为空:infogen.zookeeper");
			System.exit(-1);
		}
		kafka = infogen_properties.getProperty("infogen.kafka");
		if (kafka == null || kafka.trim().isEmpty()) {
			logger.error("kafka配置不能为空:infogen.kafka");
			System.exit(-1);
		}
		// config
		// server
		register_server.setName(infogen_properties.getProperty("infogen.name"));
		register_server.setPath(InfoGen_ZooKeeper.path(register_server.getName()));
		register_server.setDescribe(infogen_properties.getProperty("infogen.describe"));
		register_server.setProtocol(infogen_properties.getProperty("infogen.protocol"));
		register_server.setHttp_domain(infogen_properties.getProperty("infogen.http.domain"));
		register_server.setHttp_proxy(infogen_properties.getProperty("infogen.http.proxy"));
		if (!register_server.available()) {
			logger.error("服务配置不能为空:infogen.name,infogen.protocol");
			System.exit(-1);
		}
		// node
		register_node.setHost(System.getProperty("user.name").concat("@").concat(Tool_Core.getHostName()));
		String localIP = infogen_properties.getProperty("infogen.ip");
		if (localIP == null || localIP.trim().isEmpty() || !Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(localIP).find()) {
			localIP = Tool_Core.getLocalIP();
		}
		register_node.setIp(localIP);
		register_node.setName(localIP.concat("-" + Clock.system(zoneid).millis()));
		String net_ip = infogen_properties.getProperty("infogen.net_ip");
		if (net_ip == null || net_ip.trim().isEmpty() || !Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(net_ip).find()) {
		} else {
			register_node.setNet_ip(net_ip);
		}

		Integer ratio = 10;
		String ratio0 = infogen_properties.getProperty("infogen.ratio");
		if (ratio0 != null) {
			ratio = Integer.valueOf(ratio0);
			ratio = Math.min(10, ratio);
			ratio = Math.max(0, ratio);
		}
		register_node.setRatio(ratio);
		register_node.setHttp_protocol(infogen_properties.getProperty("infogen.http.protocol"));
		register_node.setContext(infogen_properties.getProperty("infogen.http.context"));
		String http_port = infogen_properties.getProperty("infogen.http.port");
		String rpc_port = infogen_properties.getProperty("infogen.rpc.port");
		if (http_port != null) {
			register_node.setHttp_port(Integer.valueOf(http_port));
			this.http_port = Integer.valueOf(http_port);
		}
		if (rpc_port != null) {
			register_node.setRpc_port(Integer.valueOf(rpc_port));
			this.rpc_port = Integer.valueOf(rpc_port);
		}
		register_node.setServer_room(infogen_properties.getProperty("infogen.server_room"));
		register_node.setTime(new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis()));

		if (!register_node.available()) {
			logger.error("节点配置不可用:infogen.server_room,infogen.ratio,infogen.ip,infogen.http.port,infogen.rpc.port");
			System.exit(-1);
		}

		// 启动 mvc 框架
		String spring_mvc_path = infogen_properties.getProperty("infogen.http.spring_mvc.path");
		String spring_mvc_mapping = infogen_properties.getProperty("infogen.http.spring_mvc.mapping");
		if (spring_mvc_path != null && !spring_mvc_path.trim().isEmpty()) {
			InfoGen_MVC_Listener.start_mvc(spring_mvc_path, spring_mvc_mapping);
		}
		// 读取白名单配置
		String infogen_security_name = infogen_properties.getProperty("infogen.security.name");
		if (infogen_security_name == null || infogen_security_name.isEmpty()) {
		} else {
			this.infogen_security_name = "security/".concat(infogen_security_name);
		}

		// 遍历项目所有class文件
		classes = find_all_class();
		classes.add(com.infogen.Service.class);
		// 读取自描述
		Map<String, Function> functions = InfoGen_Self_Describing.getInstance().self_describing(classes);
		register_server.setFunctions(functions);

	}

	// ///////////////////////////////////////////////////////////component_scan/////////////////////////////////////////////////
	public Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
	private Pattern anonymous_inner_class_compile = Pattern.compile("^*[$][0-9]+\\.class$");

	@SuppressWarnings("resource")
	private Set<Class<?>> find_all_class() throws IOException {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		String get_class_path = NativePath.get_class_path();
		if (get_class_path.endsWith(".jar")) {
			Enumeration<JarEntry> entries = new JarFile(get_class_path).entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String class_name = entry.getName();
				if (!class_name.toString().endsWith(".class") && !anonymous_inner_class_compile.matcher(class_name).find()) {
					continue;
				}
				class_name = class_name.replace(".class", "").replace("/", ".");
				try {
					classes.add(Thread.currentThread().getContextClassLoader().loadClass(class_name));
				} catch (ClassNotFoundException e) {
					logger.info("加载class失败:");
					e.printStackTrace();
				}
			}
		} else {
			Files.walk(Paths.get(get_class_path)).filter((path) -> {
				String path_string = path.toString();
				return path_string.endsWith(".class") && !anonymous_inner_class_compile.matcher(path_string).find();
			}).forEach((name) -> {
				String class_name = name.toString();
				if (System.getProperty("os.name").indexOf("Windows") != -1) {
					class_name = class_name.substring(class_name.indexOf("\\classes\\") + 9).replace(".class", "").replace("\\", ".");
				} else {
					class_name = class_name.substring(class_name.indexOf("/classes/") + 9).replace(".class", "").replace("/", ".");
				}
				try {
					classes.add(Thread.currentThread().getContextClassLoader().loadClass(class_name));
				} catch (Exception e) {
					logger.info("加载class失败:");
					e.printStackTrace();
				}
			});
		}
		return classes;
	}
}
