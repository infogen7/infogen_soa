package com.infogen;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.aop.AOP;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.mvc_framework.InfoGen_Spring;
import com.infogen.server.management.InfoGen_Loaded_Handle_Server;
import com.infogen.server.management.InfoGen_Server_Management;
import com.infogen.server.model.RemoteServer;
import com.infogen.tracking.annotation.Execution;
import com.infogen.tracking.event_handle.InfoGen_AOP_Handle_Execution;

/**
 * 启动infogen服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:58:57
 * @since 1.0
 * @version 1.0
 */
public class InfoGen {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen.class.getName());

	private static class InnerInstance {
		public static final InfoGen instance = new InfoGen();
	}

	public static InfoGen getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen() {
	}

	public static final String VERSION = "V2.5.00R151230";
	private InfoGen_Server_Management CACHE_SERVER = InfoGen_Server_Management.getInstance();
	private InfoGen_Configuration infogen_configuration = null;

	// //////////////////////////////////////////初始化/////////////////////////////////////////////////////
	private Boolean start_and_watch = false;

	public InfoGen start_and_watch(InfoGen_Configuration infogen_configuration) throws IOException, URISyntaxException {
		if (start_and_watch) {
			LOGGER.warn("InfoGen 已经启动并开启监听服务");
			return this;
		}
		start_and_watch = true;

		this.infogen_configuration = infogen_configuration;
		// AOP
		AOP.getInstance().add_advice_method(Execution.class, new InfoGen_AOP_Handle_Execution());
		AOP.getInstance().advice();

		LOGGER.info("InfoGen启动并开启监听服务");
		// 初始化缓存的服务
		CACHE_SERVER.init(infogen_configuration, () -> {// zookeeper 因连接session过期重启后定制处理
			register();
			// 这期间漏掉的Watch消息回调无法恢复 重新加载所有的服务和配置
			CACHE_SERVER.reload_all_server_flag = true;
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			LOGGER.info("InfoGen关闭并关闭监听服务");
		}));
		return this;
	}

	// 注册当前服务
	public InfoGen register() {
		LOGGER.info("注册当前服务");
		CACHE_SERVER.create_server(infogen_configuration.register_server);
		CACHE_SERVER.create_node(infogen_configuration.register_node);
		CACHE_SERVER.create_service_functions(infogen_configuration.service_functions);
		return this;
	}

	// ////////////////////////////////////////// 获取服务/////////////////////////////////////////////////////
	// 获取一个服务的缓存数据,如果没有则初始化拉取这个服务,并指定节点拉取完成的事件
	public RemoteServer get_server(String server_name) {
		RemoteServer server = CACHE_SERVER.depend_server.get(server_name);
		if (server != null) {
			return server;
		}
		return init_server(server_name, (native_server) -> {
		});
	}

	// 获取一个服务的缓存数据,如果没有则初始化拉取这个服务,并指定节点拉取完成的事件
	public RemoteServer get_server(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		RemoteServer server = CACHE_SERVER.depend_server.get(server_name);
		if (server != null) {
			return server;
		}
		return init_server(server_name, server_loaded_handle);
	}

	// 初始化服务,每个服务只会拉取一次
	private RemoteServer init_server(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		RemoteServer server = CACHE_SERVER.cache_server_single(server_name, server_loaded_handle);
		if (server != null) {
			return server;
		}
		LOGGER.warn("没有找到可用服务:".concat(server_name));
		return server;
	}
}
