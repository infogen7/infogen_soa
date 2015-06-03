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
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.infogen.aop.annotation.Authc;
import com.infogen.aop.annotation.Execution;
import com.infogen.aop.annotation.Invoke;
import com.infogen.aop.event_handle.InfoGen_AOP_Handle_Authc;
import com.infogen.aop.event_handle.InfoGen_AOP_Handle_Execution;
import com.infogen.aop.event_handle.InfoGen_AOP_Handle_Invoke;
import com.infogen.self_describing.InfoGen_Self_Describing;
import com.infogen.self_describing.component.Function;
import com.infogen.self_describing.component.OutParameter;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.web.InfoGen_Server_Initializer;
import com.infogen.zookeeper.InfoGen_ZooKeeper;
import com.larrylgq.aop.AOP;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.util.NativePath;

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

	// ////////////////////////////////////////////读取自身配置/////////////////////////////////////////////
	public RegisterNode register_node = new RegisterNode();
	public RegisterServer register_server = new RegisterServer();

	public String zookeeper;
	public String kafka;

	public Integer http_port;
	public Integer rpc_port;

	public InfoGen_Configuration(String infogen_path) throws IOException, URISyntaxException {
		Properties infogen_properties = new Properties();
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get(infogen_path), StandardOpenOption.READ);//
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
		register_node.setHttp_protocol(infogen_properties.getProperty("infogen.http.protocol"));
		register_node.setContext(infogen_properties.getProperty("infogen.http.context"));
		register_node.setServer_room(infogen_properties.getProperty("infogen.server_room"));
		register_node.setTime(new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis()));
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
		String string_http_port = infogen_properties.getProperty("infogen.http.port");
		if (string_http_port != null) {
			http_port = Integer.valueOf(string_http_port);
			register_node.setHttp_port(http_port);
		}
		String string_rpc_port = infogen_properties.getProperty("infogen.rpc.port");
		if (string_rpc_port != null) {
			rpc_port = Integer.valueOf(string_rpc_port);
			register_node.setRpc_port(rpc_port);
		}

		if (!register_node.available()) {
			logger.error("节点配置不可用:infogen.server_room,infogen.ratio,infogen.ip,infogen.http.port,infogen.rpc.port");
			System.exit(-1);
		}

		// 启动 mvc 框架
		String spring_mvc_path = infogen_properties.getProperty("infogen.http.spring_mvc.path");
		String spring_mvc_mapping = infogen_properties.getProperty("infogen.http.spring_mvc.mapping");
		if (spring_mvc_path != null && !spring_mvc_path.trim().isEmpty()) {
			InfoGen_Server_Initializer.start_mvc(spring_mvc_path, spring_mvc_mapping);
		}
		// @Resource(name="sqliteCarDao")
		// 遍历项目所有class文件
		AOP.getInstance().addClasses(com.infogen.Service.class);
		// 读取自描述
		Map<String, Function> functions = InfoGen_Self_Describing.getInstance().self_describing(AOP.getInstance().getClasses());
		register_server.setFunctions(functions);

		// AOP
		AOP.getInstance().add_advice_method(Execution.class, new InfoGen_AOP_Handle_Execution());
		AOP.getInstance().add_advice_method(Invoke.class, new InfoGen_AOP_Handle_Invoke());
		AOP.getInstance().add_advice_method(Authc.class, new InfoGen_AOP_Handle_Authc());
		AOP.getInstance().advice();
	}
}
