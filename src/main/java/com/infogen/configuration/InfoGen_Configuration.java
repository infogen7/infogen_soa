package com.infogen.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.InfoGen;
import com.infogen.aop.AOP;
import com.infogen.core.tools.Tool_Core;
import com.infogen.core.util.NativePath;
import com.infogen.http.mvc_framework.InfoGen_Server_Initializer;
import com.infogen.self_description.InfoGen_Self_Description;
import com.infogen.self_description.component.Function;
import com.infogen.self_description.component.OutParameter;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.server.zookeeper.InfoGen_ZooKeeper;
import com.infogen.tracking.annotation.Execution;
import com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution;

/**
 * infogen配置解析及其它全局配置
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 * @version 创建时间 2013-9-30 下午1:57:58
 */

public class InfoGen_Configuration {
	private final static Logger LOGGER = Logger.getLogger(InfoGen_Configuration.class.getName());

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
	// ////////////////////////////////////////////读取自身配置/////////////////////////////////////////////

	public String zookeeper;
	public String kafka;

	public InfoGen_Configuration add_basic_outparameter(OutParameter basic_outparameter) {
		for (Function function : register_server.getHttp_functions().values()) {
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
			LOGGER.warn("kafka配置为空:infogen.kafka");
		}
		// server
		register_server.setInfogen_version(InfoGen.VERSION);
		register_server.setName(infogen_properties.getProperty("infogen.name"));
		register_server.setPath(InfoGen_ZooKeeper.path(register_server.getName()));
		register_server.setDescribe(infogen_properties.getProperty("infogen.describe"));
		String min_nodes = infogen_properties.getProperty("infogen.min_nodes");
		register_server.setMin_nodes((min_nodes == null) ? 1 : Integer.valueOf(min_nodes));
		register_server.setProtocol(infogen_properties.getProperty("infogen.protocol"));
		register_server.setHttp_domain(infogen_properties.getProperty("infogen.http.domain"));
		register_server.setHttp_proxy(infogen_properties.getProperty("infogen.http.proxy"));
		register_server.setHttp_functions(InfoGen_Self_Description.getInstance().self_description(AOP.getInstance().getClasses()));// 自描述
		if (!register_server.available()) {
			LOGGER.error("服务配置不能为空:infogen.name");
			System.exit(-1);
		}

		// node
		String localIP = infogen_properties.getProperty("infogen.ip");
		if (localIP == null || localIP.trim().isEmpty() || !Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(localIP).find()) {
			localIP = Tool_Core.getLocalIP();
		}
		register_node.setName(localIP.concat("-" + Clock.system(zoneid).millis()));
		register_node.setIp(localIP);
		String net_ip = infogen_properties.getProperty("infogen.net_ip");
		if (net_ip != null && !net_ip.trim().isEmpty() && Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(net_ip).find()) {
			register_node.setNet_ip(net_ip);
		}
		String http_port = infogen_properties.getProperty("infogen.http.port");
		register_node.setHttp_port((http_port == null) ? null : Integer.valueOf(http_port));
		String rpc_port = infogen_properties.getProperty("infogen.rpc.port");
		register_node.setRpc_port((rpc_port == null) ? null : Integer.valueOf(rpc_port));
		register_node.setHost(System.getProperty("user.name").concat("@").concat(Tool_Core.getHostName()));
		String ratio = infogen_properties.getProperty("infogen.ratio");
		register_node.setRatio((ratio == null) ? 10 : Math.max(0, Math.min(10, Integer.valueOf(ratio))));
		register_node.setPath(InfoGen_ZooKeeper.path(register_server.getName()).concat("/".concat(register_node.getName())));
		register_node.setHttp_protocol(infogen_properties.getProperty("infogen.http.protocol"));
		register_node.setContext(infogen_properties.getProperty("infogen.http.context"));
		register_node.setServer_room(infogen_properties.getProperty("infogen.server_room"));
		register_node.setTime(new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis()));
		String node_version = infogen_properties.getProperty("infogen.node_version");
		if(node_version!=null&&
				!node_version.isEmpty()){
			register_node.setNode_version(node_version);
		}
		

		if (!register_node.available()) {
			LOGGER.error("节点配置配置不能为空:infogen.name,infogen.ratio,infogen.ip,infogen.http.port或infogen.rpc.port");
			System.exit(-1);
		}

		// /////////////////////////////////////////////////////初始化启动配置/////////////////////////////////////////////////////////////////////

		// 添加infogen自己的类到AOP的类集合
		AOP.getInstance().addClasses(com.infogen.Service.class);
		// AOP
		AOP.getInstance().add_advice_method(Execution.class, new InfoGen_AOP_Handle_Execution());

		// 延迟启动 mvc 框架
		String spring_mvc_path = infogen_properties.getProperty("infogen.http.spring_mvc.path");
		String spring_mvc_mapping = infogen_properties.getProperty("infogen.http.spring_mvc.mapping");
		if (spring_mvc_path != null && !spring_mvc_path.trim().isEmpty()) {
			InfoGen_Server_Initializer.start_mvc(spring_mvc_path, spring_mvc_mapping);
		}
		return this;
	}
}
