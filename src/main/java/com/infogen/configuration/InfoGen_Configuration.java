package com.infogen.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import com.infogen.InfoGen;
import com.infogen.aop.AOP;
import com.infogen.core.structure.DefaultEntry;
import com.infogen.core.tools.Tool_Core;
import com.infogen.core.util.NativePath;
import com.infogen.http.self_description.HTTP_Parser;
import com.infogen.rpc.annotation.RPCController;
import com.infogen.rpc.self_description.RPC_Parser;
import com.infogen.self_description.InfoGen_Self_Description;
import com.infogen.self_description.Self_Description;
import com.infogen.self_description.component.Function;
import com.infogen.self_description.component.OutParameter;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.server.model.ServiceFunctions;

/**
 * infogen配置解析及其它全局配置
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:54:26
 * @since 1.0
 * @version 1.0
 */

public class InfoGen_Configuration {
	private final static Logger LOGGER = LogManager.getLogger(InfoGen_Configuration.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Configuration instance = new InfoGen_Configuration();
	}

	public static InfoGen_Configuration getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Configuration() {
	}

	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

	public final RegisterNode register_node = new RegisterNode();
	public final RegisterServer register_server = new RegisterServer();
	public final ServiceFunctions service_functions = new ServiceFunctions();
	// ////////////////////////////////////////////读取自身配置/////////////////////////////////////////////

	public String zookeeper;
	public String kafka;
	public String mapping_path;
	public String mapping_pattern;

	public InfoGen_Configuration add_basic_outparameter(OutParameter basic_outparameter) {
		for (Function function : service_functions.getHttp_functions()) {
			function.getOut_parameters().add(basic_outparameter);
		}
		return this;
	}

	// ///////////////////////////////////// initialization //////////////////////////////////////////
	public InfoGen_Configuration initialization(String infogen_path) throws IOException, URISyntaxException {
		Properties infogen_properties = new Properties();
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get(infogen_path), StandardOpenOption.READ); //
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);) {
			infogen_properties.load(inputstreamreader);
		}
		return initialization(infogen_properties);
	}

	public InfoGen_Configuration initialization(Properties infogen_properties) throws IOException, URISyntaxException {
		zookeeper = infogen_properties.getProperty("infogen.zookeeper");
		if (zookeeper == null || zookeeper.trim().isEmpty()) {
			LOGGER.error("zookeeper配置不能为空:infogen.zookeeper");
			System.exit(-1);
		}
		kafka = infogen_properties.getProperty("infogen.kafka");
		if (kafka == null || kafka.trim().isEmpty()) {
			LOGGER.warn("kafka配置为空:infogen.kafka 调用链/日志等功能将不可用");
		}
		mapping_path = infogen_properties.getProperty("infogen.http.spring_mvc.path");
		mapping_pattern = infogen_properties.getProperty("infogen.http.spring_mvc.mapping");

		// server
		register_server.setInfogen_version(InfoGen.VERSION);
		register_server.setName(infogen_properties.getProperty("infogen.name"));
		register_server.setDescribe(infogen_properties.getProperty("infogen.describe"));
		String min_nodes = infogen_properties.getProperty("infogen.min_nodes");
		register_server.setMin_nodes((min_nodes == null) ? 1 : Integer.valueOf(min_nodes));
		register_server.setProtocol(infogen_properties.getProperty("infogen.protocol"));
		register_server.setHttp_proxy(infogen_properties.getProperty("infogen.http.proxy"));

		// server - 自描述
		if (!register_server.available()) {
			LOGGER.error("服务配置不能为空:infogen.name");
			System.exit(-1);
		}

		// node
		String localIP = infogen_properties.getProperty("infogen.ip");
		if (localIP == null || localIP.trim().isEmpty() || !Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(localIP).find()) {
			String ifcfgs = infogen_properties.getProperty("infogen.ifcfgs");
			localIP = Tool_Core.getLocalIP(Tool_Core.trim((ifcfgs == null || ifcfgs.trim().isEmpty()) ? "eth,wlan" : ifcfgs).split(","));
		}
		register_node.setIp(localIP);
		String net_ip = infogen_properties.getProperty("infogen.net_ip");
		if (net_ip != null && !net_ip.trim().isEmpty() && Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(net_ip).find()) {
			register_node.setNet_ip(net_ip);
		}

		register_node.setName(localIP.concat("-" + Clock.system(zoneid).millis()));
		register_node.setServer_name(register_server.getName());
		String http_port = infogen_properties.getProperty("infogen.http.port");
		register_node.setHttp_port((http_port == null) ? null : Integer.valueOf(http_port));
		String rpc_port = infogen_properties.getProperty("infogen.rpc.port");
		register_node.setRpc_port((rpc_port == null) ? null : Integer.valueOf(rpc_port));
		register_node.setHost(System.getProperty("user.name").concat("@").concat(Tool_Core.getHostName()));
		String ratio = infogen_properties.getProperty("infogen.ratio");
		register_node.setRatio((ratio == null) ? 10 : Math.max(0, Math.min(10, Integer.valueOf(ratio))));
		register_node.setHttp_protocol(infogen_properties.getProperty("infogen.http.protocol"));
		register_node.setHttp_context(infogen_properties.getProperty("infogen.http.context"));
		register_node.setServer_room(infogen_properties.getProperty("infogen.server_room"));
		register_node.setTime(new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis()));

		if (!register_node.available()) {
			LOGGER.error("节点配置配置不能为空:infogen.name,infogen.ratio,infogen.ip,infogen.http.port或infogen.rpc.port");
			System.exit(-1);
		}

		// /////////////////////////////////////////////////////初始化启动配置/////////////////////////////////////////////////////////////////////
		InfoGen_Self_Description infogen_self_description = InfoGen_Self_Description.getInstance();
		List<DefaultEntry<Class<? extends Annotation>, Self_Description>> defaultentrys = new ArrayList<>();
		defaultentrys.add(new DefaultEntry<Class<? extends Annotation>, Self_Description>(RestController.class, new HTTP_Parser()));
		defaultentrys.add(new DefaultEntry<Class<? extends Annotation>, Self_Description>(RPCController.class, new RPC_Parser()));
		service_functions.getHttp_functions().addAll(infogen_self_description.self_description(AOP.getInstance().getClasses(), defaultentrys));
		service_functions.setServer(register_server);
		return this;
	}
}
