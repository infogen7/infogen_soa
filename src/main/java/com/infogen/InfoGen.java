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

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.server.cache.InfoGen_Cache_Server;
import com.infogen.server.cache.InfoGen_Loaded_Handle_Server;
import com.infogen.server.model.RemoteServer;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.server.zookeeper.InfoGen_ZooKeeper;
import com.larrylgq.aop.tools.Tool_Jackson;

/**
 * 启动infogen服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:58:57
 * @since 1.0
 * @version 1.0
 */
public class InfoGen {
	private static final Logger LOGGER = Logger.getLogger(InfoGen.class.getName());

	private static class InnerInstance {
		public static final InfoGen instance = new InfoGen();
	}

	public static InfoGen getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen() {
	}

	public static final String VERSION = "V1.1.00R150717";
	private InfoGen_ZooKeeper ZK = com.infogen.server.zookeeper.InfoGen_ZooKeeper.getInstance();
	private InfoGen_Cache_Server CACHE_SERVER = InfoGen_Cache_Server.getInstance();

	// //////////////////////////////////////////初始化/////////////////////////////////////////////////////
	public InfoGen start_and_watch(InfoGen_Configuration infogen_configuration) throws IOException, URISyntaxException {
		// 初始化缓存的服务
		CACHE_SERVER.init(infogen_configuration, () -> {// zookeeper 因连接session过期重启后定制处理
					register();
					// 这期间漏掉的Watch消息回调无法恢复 重新加载所有的服务和配置
				CACHE_SERVER.reload_all_server();
			});
		return this;
	}

	/**
	 * 注册当前服务
	 * 
	 * @return
	 */
	public InfoGen register() {
		create_server(InfoGen_Configuration.register_server);
		create_node(InfoGen_Configuration.register_node);
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
			LOGGER.error("注册配置失败");
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
			LOGGER.error("注册自身服务失败!");
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
			LOGGER.error("注册自身节点失败!");
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
	public RemoteServer get_server(String server_name) {
		RemoteServer server = CACHE_SERVER.depend_server.get(server_name);
		if (server != null) {
			return server;
		}
		return init_server(server_name, (native_server) -> {
		});
	}

	/**
	 * 获取一个服务的缓存数据,如果没有则初始化拉取这个服务,并指定节点拉取完成的事件
	 * 
	 * @param server_name
	 * @param server_loaded_handle
	 * @return
	 */
	public RemoteServer get_server(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		RemoteServer server = CACHE_SERVER.depend_server.get(server_name);
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
	private RemoteServer init_server(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		RemoteServer server = CACHE_SERVER.cache_server_single(server_name, server_loaded_handle);
		if (server != null) {
			return server;
		}
		LOGGER.warn("没有找到可用服务:".concat(server_name));
		
		if (server_loaded_handle != null) {
			server_loaded_handle.handle_event(server);
		}
		return server;
	}
}
