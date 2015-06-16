package com.infogen;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.infogen.cache.InfoGen_Cache_Configuration;
import com.infogen.cache.InfoGen_Cache_Server;
import com.infogen.cache.event_handle.InfoGen_Loaded_Handle_Configuration;
import com.infogen.cache.event_handle.InfoGen_Loaded_Handle_Server;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.server.model.NativeServer;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.zookeeper.InfoGen_ZooKeeper;
import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * 启动infogen服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:58:57
 * @since 1.0
 * @version 1.0
 */
public class InfoGen {
	public final Logger logger = Logger.getLogger(InfoGen.class.getName());

	private static class InnerInstance {
		public static InfoGen instance = new InfoGen();
	}

	public static InfoGen getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen() {
	}

	private InfoGen_ZooKeeper ZK = com.infogen.zookeeper.InfoGen_ZooKeeper.getInstance();
	private InfoGen_Cache_Configuration CACHE_CONFIGURATION = InfoGen_Cache_Configuration.getInstance();
	private InfoGen_Cache_Server CACHE_SERVER = InfoGen_Cache_Server.getInstance();
	private InfoGen_Configuration configuration;

	// //////////////////////////////////////////初始化/////////////////////////////////////////////////////
	public InfoGen start_and_watch(InfoGen_Configuration infogen_configuration) throws IOException, URISyntaxException {
		this.configuration = infogen_configuration;
		// 初始化 zookeeper
		ZK.start_zookeeper(infogen_configuration.zookeeper, () -> {// zookeeper 因连接session过期重启后定制处理
					register();
					// TODO 这期间漏掉的Watch消息回调无法恢复 重新加载所有的服务和配置
			});
		// 定时修正监听失败
		ZK.schedule();
		// 读取本地缓存的服务
		CACHE_SERVER.init_local_infogen_cfg_dynamic();
		// 定时修正没有找到可用服务
		CACHE_SERVER.schedule();
		// 读取本地缓存的配置
		CACHE_CONFIGURATION.init_local_infogen_cfg_dynamic();
		// 定时修正没有找到可用配置
		CACHE_CONFIGURATION.schedule();
		return this;
	}

	/**
	 * 注册当前服务
	 * 
	 * @return
	 */
	public InfoGen register() {
		create_server(configuration.register_server);
		create_node(configuration.register_node);
		return this;
	}

	/**
	 * 写入或更新一个配置
	 * 
	 * @param configuration_name
	 * @param configuration_value
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public InfoGen upsert_configuration(String configuration_name, String configuration_value, String digest) throws NoSuchAlgorithmException {
		List<ACL> acls = new ArrayList<ACL>();
		// 采用用户名密码形式
		acls.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(digest))));
		// 所有用户可读权限
		acls.add(new ACL(ZooDefs.Perms.READ, new Id("world", "anyone")));
		// 创建或更新配置节点
		String create_path = ZK.create(InfoGen_ZooKeeper.configuration_path(configuration_name), configuration_value.getBytes(), acls, CreateMode.PERSISTENT);
		if (create_path == null) {
			logger.error("注册配置失败");
		} else if (create_path.equals(Code.NODEEXISTS.name())) {
			ZK.add_auth_info("digest", digest);
			ZK.set_data(InfoGen_ZooKeeper.configuration_path(configuration_name), configuration_value.getBytes(), -1);
		}
		return this;
	}

	// //////////////////////////////////////////////////zookeeper注册/////////////////////////////////////////////
	/**
	 * 生成一个服务节点
	 * 
	 * @param register_server
	 */
	private void create_server(RegisterServer register_server) {
		String path = register_server.getPath();
		// 创建或更新服务节点
		byte[] bytes = Tool_Jackson.toJson(register_server).getBytes();
		String create_path = ZK.create(path, bytes, CreateMode.PERSISTENT);
		if (create_path == null) {
			logger.error("注册自身服务失败!");
			return;
		} else if (create_path.equals(Code.NODEEXISTS.name())) {
			// 更新服务节点数据
			ZK.set_data(path, bytes, -1);
		}
	}

	/**
	 * 生成一个服务实例节点
	 * 
	 * @param server_name
	 * @param register_node
	 */
	private void create_node(RegisterNode register_node) {
		// 创建应用子节点及子节点数据
		String path = register_node.getPath();
		String create_path = ZK.create(path, Tool_Jackson.toJson(register_node).getBytes(), CreateMode.EPHEMERAL);
		if (create_path == null) {
			logger.error("注册自身节点失败!");
			return;
		}
	}

	// ////////////////////////////////////////// 获取服务/////////////////////////////////////////////////////
	/**
	 * 获取一个服务的缓存数据,如果没有则初始化拉取这个服务,并指定节点拉取完成的事件
	 * 
	 * @param server_name
	 * @return
	 */
	public NativeServer get_server(String server_name) {
		NativeServer server = CACHE_SERVER.depend_server.get(server_name);
		if (server != null) {
			return server;
		}
		return init_server(server_name, (native_server) -> {
			// 缓存
				CACHE_SERVER.depend_server.put(server_name, native_server);
				// 持久化
				CACHE_SERVER.persistence();
			});
	}

	/**
	 * 获取一个服务的缓存数据,如果没有则初始化拉取这个服务,并指定节点拉取完成的事件
	 * 
	 * @param server_name
	 * @param server_loaded_handle
	 * @return
	 */
	public NativeServer get_server(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		NativeServer server = CACHE_SERVER.depend_server.get(server_name);
		if (server != null) {
			return server;
		}
		return init_server(server_name, server_loaded_handle);
	}

	/**
	 * 初始化服务,每个服务只会拉取一次
	 * 
	 * @param server_name
	 * @param server_loaded_handle
	 * @return
	 */
	private NativeServer init_server(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		NativeServer server = CACHE_SERVER.cache_server_single(server_name, server_loaded_handle);
		if (server != null) {
			return server;
		}
		logger.warn("没有找到可用服务:".concat(server_name));

		// 获取不到,到本地缓存里查找 并触发 server 加载完成的事件
		server = CACHE_SERVER.depend_server_cache.get(server_name);
		if (server != null) {
			CACHE_SERVER.depend_server.put(server_name, server);
			logger.warn("使用本地缓存的服务:".concat(server_name));
		}
		if (server != null && server_loaded_handle != null) {
			server_loaded_handle.handle_event(server);
		}

		return server;
	}

	// ////////////////////////////////////////// 获取配置/////////////////////////////////////////////////////
	/**
	 * 获取配置数据,,如果没有则初始化拉取这个配置,并指定节点拉取完成的事件
	 * 
	 * @param configuration_name
	 * @return
	 */
	public String get_configuration(String configuration_name) {
		String configuration = CACHE_CONFIGURATION.depend_configuration.get(configuration_name);
		if (configuration != null) {
			return configuration;
		}
		return init_configuration(configuration_name, (data) -> {
			// 缓存
				CACHE_CONFIGURATION.depend_configuration.put(configuration_name, data);
				// 持久化
				CACHE_CONFIGURATION.persistence();
			});
	}

	/**
	 * 获取配置数据,,如果没有则初始化拉取这个配置,并指定节点拉取完成的事件
	 * 
	 * @param configuration_name
	 * @param configuration_loaded_handle
	 * @return
	 */
	public String get_configuration(String configuration_name, InfoGen_Loaded_Handle_Configuration configuration_loaded_handle) {
		String configuration = CACHE_CONFIGURATION.depend_configuration.get(configuration_name);
		if (configuration != null) {
			return configuration;
		}
		return init_configuration(configuration_name, configuration_loaded_handle);
	}

	/**
	 * 初始化配置,每个配置只会拉取一次
	 * 
	 * @param configuration_name
	 * @param configuration_loaded_handle
	 * @return
	 */
	private String init_configuration(String configuration_name, InfoGen_Loaded_Handle_Configuration configuration_loaded_handle) {
		String data = CACHE_CONFIGURATION.cache_configuration_single(configuration_name, configuration_loaded_handle);
		if (data != null) {
			return data;
		}
		logger.warn("没有找到可用配置:".concat(configuration_name));

		// 获取不到,到本地缓存里查找 并触发 configuration 加载完成的事件
		data = CACHE_CONFIGURATION.depend_configuration_cache.get(configuration_name);
		if (data != null) {
			CACHE_CONFIGURATION.depend_configuration.put(configuration_name, data);
			logger.warn("使用本地缓存的配置:".concat(configuration_name));
		}
		if (data != null && configuration_loaded_handle != null) {
			configuration_loaded_handle.handle_event(data);
		}

		return data;
	}

	// ///////////////////////////////////////////////////////////////////getter setter/////////////////////////

	public InfoGen_Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(InfoGen_Configuration configuration) {
		this.configuration = configuration;
	}

}
