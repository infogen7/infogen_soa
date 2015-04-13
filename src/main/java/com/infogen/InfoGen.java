/**
 * 
 */
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
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.infogen.aop.InfoGen_AOP;
import com.infogen.cache.InfoGen_Configuration_Cache;
import com.infogen.cache.InfoGen_Server_Cache;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.event_handle.Configuration_Loaded_Handle;
import com.infogen.event_handle.Server_Loaded_Handle;
import com.infogen.security.InfoGen_Security;
import com.infogen.security.component.Security;
import com.infogen.server.NativeServer;
import com.infogen.server.RegisterNode;
import com.infogen.server.RegisterServer;
import com.infogen.tools.Tool_Jackson;
import com.infogen.zookeeper.InfoGen_ZooKeeper;

/**
 * 启动infogen服务
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月27日 下午2:57:52
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
	private InfoGen_Configuration_Cache CACHE_CONFIGURATION = InfoGen_Configuration_Cache.getInstance();
	private InfoGen_Server_Cache CACHE_SERVER = InfoGen_Server_Cache.getInstance();
	private InfoGen_Configuration configuration;

	// //////////////////////////////////////////初始化/////////////////////////////////////////////////////
	public InfoGen start_and_watch(InfoGen_Configuration infogen_configuration) throws IOException, URISyntaxException {
		this.configuration = infogen_configuration;
		// AOP
		infogen_configuration.classes.forEach((clazz) -> {
			InfoGen_AOP.getInstance().attach(clazz);
		});
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
		// 获取白名单配置
		if (configuration.infogen_security_name != null) {
			Configuration_Loaded_Handle configuration_loaded_handle = (security) -> {
				try {
					Security securitys = Tool_Jackson.toObject(security, Security.class);
					InfoGen_Security.getInstance().refresh_security(securitys);
					// 缓存
					CACHE_CONFIGURATION.depend_configuration.put(configuration.infogen_security_name, security);
					// 持久化
					CACHE_CONFIGURATION.persistence();
				} catch (Exception e) {
					logger.error("更新安全配置失败", e);
				}
			};
			get_configuration(configuration.infogen_security_name, configuration_loaded_handle);
		} else {
			List<String> white_lists = new ArrayList<>();
			white_lists.add("/*");
			InfoGen_Security.getInstance().add_ignore(white_lists);
		}
		return this;
	}

	/**
	 * 注册当前服务
	 * 
	 * @return
	 */
	public InfoGen register() {
		RegisterServer register_server = configuration.register_server;
		create_server(register_server);
		create_node(register_server.getName(), configuration.register_node);
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
		String register_server_name = register_server.getName();
		// 创建或更新服务节点
		byte[] bytes = Tool_Jackson.toJson(register_server).getBytes();
		String create_path = ZK.create(InfoGen_ZooKeeper.path(register_server_name), bytes, CreateMode.PERSISTENT);
		if (create_path == null) {
			logger.error("注册自身服务失败!");
			return;
		} else if (create_path.equals(Code.NODEEXISTS.name())) {
			// 更新服务节点数据
			ZK.set_data(InfoGen_ZooKeeper.path(register_server_name), bytes, -1);
		}
	}

	/**
	 * 生成一个服务实例节点
	 * 
	 * @param server_name
	 * @param register_node
	 */
	private void create_node(String server_name, RegisterNode register_node) {
		// 创建应用子节点及子节点数据
		String path = InfoGen_ZooKeeper.path(server_name).concat("/".concat(register_node.getName()));
		register_node.setPath(path);
		String create_path = ZK.create(path, Tool_Jackson.toJson(register_node).getBytes(), CreateMode.EPHEMERAL);
		if (create_path == null) {
			logger.error("注册自身节点失败!");
			return;
		}
	}

	/**
	 * 代理创建一个服务节点
	 * 
	 * @param register_server
	 */
	@Deprecated
	public void create_proxy_server(RegisterServer register_server) {
		String register_server_name = register_server.getName();
		// 创建或更新服务节点
		String create_path = ZK.create(InfoGen_ZooKeeper.path(register_server_name), new byte[0], CreateMode.PERSISTENT);
		if (create_path == null) {
			logger.error("注册自身服务失败!");
			return;
		}
		if (create_path != "NODEEXISTS") {
			// 更新服务节点数据
			ZK.set_data(create_path, Tool_Jackson.toJson(register_server).getBytes(), -1);
		}
	}

	/**
	 * 代理创建一个服务实例节点
	 * 
	 * @param server_name
	 * @param proxy_node
	 */
	@Deprecated
	public void create_proxy_node(String server_name, RegisterNode proxy_node) {
		Stat server_exists = ZK.exists(InfoGen_ZooKeeper.path(server_name));
		if (server_exists == null) {
			RegisterServer register_server = new RegisterServer();
			register_server.setName(server_name);
			create_proxy_server(register_server);
		}

		String node_path = InfoGen_ZooKeeper.path(server_name, proxy_node.getName());
		Stat exists = ZK.exists(node_path);
		if (exists != null) {
			// 代理节点都是持久化节点,所以需要先删除后创建才会出发 getchilds 的监听
			ZK.delete(node_path);
		}
		ZK.create(node_path, Tool_Jackson.toJson(proxy_node).getBytes(), CreateMode.PERSISTENT);
	}

	/**
	 * 删除一个服务实例节点
	 * 
	 * @param server_name
	 * @param node_name
	 */
	@Deprecated
	public void delete_proxy_node(String server_name, String node_name) {
		ZK.delete(InfoGen_ZooKeeper.path(server_name, node_name));
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
			// 获取所有节点数据并转化为本地调用版本
				native_server.rehash();
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
	public NativeServer get_server(String server_name, Server_Loaded_Handle server_loaded_handle) {
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
	private NativeServer init_server(String server_name, Server_Loaded_Handle server_loaded_handle) {
		NativeServer server = CACHE_SERVER.cache_server_single(server_name, server_loaded_handle);
		if (server != null) {
			return server;
		}
		logger.warn("没有找到可用服务:".concat(server_name));

		// 获取不到,到本地缓存里查找 并触发 server 加载完成的事件
		server = CACHE_SERVER.depend_server_cache.get(server_name);
		if (server != null) {
			CACHE_SERVER.depend_server.put(server_name, server);
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
	public String get_configuration(String configuration_name, Configuration_Loaded_Handle configuration_loaded_handle) {
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
	private String init_configuration(String configuration_name, Configuration_Loaded_Handle configuration_loaded_handle) {
		String data = CACHE_CONFIGURATION.cache_configuration_single(configuration_name, configuration_loaded_handle);
		if (data != null) {
			return data;
		}
		logger.warn("没有找到可用配置:".concat(configuration_name));

		// 获取不到,到本地缓存里查找 并触发 configuration 加载完成的事件
		data = CACHE_CONFIGURATION.depend_configuration_cache.get(configuration_name);
		if (data != null) {
			CACHE_CONFIGURATION.depend_configuration.put(configuration_name, data);
		}
		if (data != null && configuration_loaded_handle != null) {
			configuration_loaded_handle.handle_event(data);
		}

		return data;
	}
}
