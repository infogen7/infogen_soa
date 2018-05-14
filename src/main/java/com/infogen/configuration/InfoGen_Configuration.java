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
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import com.infogen.InfoGen;
import com.infogen.path.NativePath;
import com.infogen.rpc.annotation.RPCController;
import com.infogen.self_description.InfoGen_Parser_HTTP;
import com.infogen.self_description.InfoGen_Parser_RPC;
import com.infogen.self_description.InfoGen_Self_Description;
import com.infogen.self_description.component.Function;
import com.infogen.self_description.component.OutParameter;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.server.model.ServiceFunctions;
import com.infogen.tools.IP;

/**
 * infogen配置解析及其它全局配置
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:54:26
 * @since 1.0
 * @version 1.0
 */

public class InfoGen_Configuration {
	private final static Logger LOGGER = LogManager.getLogger(InfoGen_Configuration.class.getName());

	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

	public String zookeeper;
	public final RegisterNode register_node = new RegisterNode();
	public final RegisterServer register_server = new RegisterServer();
	public final ServiceFunctions service_functions = new ServiceFunctions();
	// ////////////////////////////////////////////读取自身配置/////////////////////////////////////////////

	public InfoGen_Configuration add_basic_outparameter(OutParameter basic_outparameter) {
		for (Function function : service_functions.getFunctions()) {
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
		LOGGER.info("#读取InfoGen 配置:");
		infogen_properties.forEach((k, v) -> {
			LOGGER.info(k + "=" + v);
		});

		zookeeper = infogen_properties.getProperty("infogen.zookeeper");
		if (zookeeper == null || zookeeper.trim().isEmpty()) {
			LOGGER.warn("zookeeper配置为空:SOA功能将不可用");
		}

		// server
		register_server.setInfogen_version(InfoGen.VERSION);
		register_server.setName(infogen_properties.getProperty("infogen.name"));
		register_server.setDescribe(infogen_properties.getProperty("infogen.describe"));
		if (!register_server.available()) {
			LOGGER.error("服务配置不能为空:infogen.name");
			System.exit(-1);
		}

		// node
		String localIP = infogen_properties.getProperty("infogen.ip");
		if (localIP == null || localIP.trim().isEmpty() || !Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(localIP).find()) {
			String ifcfgs = infogen_properties.getProperty("infogen.ifcfgs");
			localIP = IP.get_local_ip(((ifcfgs == null || ifcfgs.trim().isEmpty()) ? "eth,wlan" : ifcfgs).split(","));
		}
		LOGGER.info("localIP :" + localIP);
		register_node.setIp(localIP);
		register_node.setName(localIP.concat("-" + Clock.system(zoneid).millis()));
		register_node.setServer_name(register_server.getName());
		String http_port = infogen_properties.getProperty("infogen.http.port");
		register_node.setHttp_port((http_port == null) ? 8080 : Integer.valueOf(http_port));
		String rpc_port = infogen_properties.getProperty("infogen.rpc.port");
		register_node.setRpc_port((rpc_port == null) ? 18080 : Integer.valueOf(rpc_port));
		register_node.setHost(System.getProperty("user.name").concat("@").concat(IP.get_hostname()));
		String ratio = infogen_properties.getProperty("infogen.ratio");
		register_node.setRatio((ratio == null) ? 10 : Math.max(0, Math.min(10, Integer.valueOf(ratio))));
		register_node.setTime(new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis()));

		if (!register_node.available()) {
			LOGGER.warn("节点配置配置为空:infogen.http.port或infogen.rpc.port:http或rpc功能将不可用");
		}

		// /////////////////////////////////////////////////////自描述/////////////////////////////////////////////////////////////////////
		InfoGen_Self_Description.getInstance().add_parser(RestController.class, new InfoGen_Parser_HTTP());
		InfoGen_Self_Description.getInstance().add_parser(RPCController.class, new InfoGen_Parser_RPC());
		List<Function> functions = InfoGen_Self_Description.getInstance().parser();
		service_functions.getFunctions().addAll(functions);
		service_functions.setServer(register_server);
		return this;
	}
}
