package com.infogen.server.cache;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.tools.Tool_Core;
import com.infogen.core.tools.Tool_Jackson;
import com.infogen.core.util.NativePath;
import com.infogen.core.util.Scheduled;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.infogen.server.zookeeper.InfoGen_ZooKeeper;
import com.infogen.server.zookeeper.InfoGen_Zookeeper_Handle_Expired;

/**
 * 加载和缓存服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:25:01
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Cache_Server {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Cache_Server.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Cache_Server instance = new InfoGen_Cache_Server();
	}

	public static InfoGen_Cache_Server getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Cache_Server() {
		// Scheduled
		// 定时重新加载失败的服务
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			retry_cache_server();
		} , 30, 30, TimeUnit.SECONDS);
		// 定时检测并执行持久化服务
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			persistence_delay();
		} , 30, 30, TimeUnit.SECONDS);
		// 定时检测并执行重新加载所有依赖的服务
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			reload_all_server_delay();
		} , 3, 3, TimeUnit.MINUTES);
	}

	private InfoGen_ZooKeeper ZK = com.infogen.server.zookeeper.InfoGen_ZooKeeper.getInstance();
	// 依赖的服务
	public final ConcurrentMap<String, RemoteServer> depend_server = new ConcurrentHashMap<>();
	// 本地缓存的依赖服务
	public final ConcurrentMap<String, RemoteServer> depend_server_cache = new ConcurrentHashMap<>();
	private Path source_server_path = NativePath.get("infogen.cache.server.js");
	private Path target_server_path = NativePath.get("infogen.cache.server.js.copy");
	// 加载完成后触发的事件
	private Map<String, InfoGen_Loaded_Handle_Server> server_loaded_handle_map = new HashMap<>();
	// 需要重试加载的服务
	private Set<String> retry_cache_server_paths = new HashSet<>();

	// ///////////////////////////////////////////////////初始化///////////////////////////////////////////////

	public void init(InfoGen_Configuration infogen_configuration, InfoGen_Zookeeper_Handle_Expired expired_handle) throws IOException, URISyntaxException {
		// 初始化 zookeeper
		ZK.start_zookeeper(infogen_configuration.zookeeper, expired_handle);

		// 初始化所有需要的配置文件 如果不存在则创建
		Tool_Core.prepare_files(source_server_path, target_server_path);

		// 获取缓存的服务
		String server_json = Tool_Core.load_file(source_server_path);
		if (!server_json.isEmpty()) {
			ConcurrentHashMap<String, RemoteServer> fromJson = Tool_Jackson.toObject(server_json, new TypeReference<ConcurrentHashMap<String, RemoteServer>>() {
			});
			for (String key : fromJson.keySet()) {
				depend_server_cache.put(key, fromJson.get(key));
			}
		}
	}

	//////////////////////////////////////////////////////// create_update_server//////////////////////////////////////////////////////////////
	/**
	 * 写入或更新一个配置
	 * 
	 * @param configuration_name
	 * @param configuration_value
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public void upsert_configuration(String configuration_name, String configuration_value, String digest) throws NoSuchAlgorithmException {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启-InfoGen.getInstance().start_and_watch(infogen_configuration);");
			return;
		}

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
	}

	/**
	 * 生成一个服务节点
	 * 
	 * @param register_server
	 */
	public void create_server(RegisterServer register_server) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启-InfoGen.getInstance().start_and_watch(infogen_configuration);");
			return;
		}

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
	public void create_node(RegisterNode register_node) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启-InfoGen.getInstance().start_and_watch(infogen_configuration);");
			return;
		}

		// 创建应用子节点及子节点数据
		String path = register_node.getPath();
		String create_path = ZK.create(path, Tool_Jackson.toJson(register_node).getBytes(), CreateMode.EPHEMERAL);
		if (create_path == null) {
			LOGGER.error("注册自身节点失败!");
			return;
		}
	}

	// ////////////////////////////////////////////cache_server////////////////////////////////////////////////////////////////
	// 注册的cache完成事件处理器
	public RemoteServer cache_server_single(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		if (server_loaded_handle_map.get(server_name) != null) {
			LOGGER.warn("已经初始化过该服务:".concat(server_name));
			return depend_server.get(server_name);
		}

		// 注册 server 加载完成的事件
		server_loaded_handle_map.put(server_name, server_loaded_handle);

		RemoteServer cache_server = cache_server(server_name);
		// 获取不到,到本地缓存里查找
		if (cache_server == null) {
			cache_server_local(server_name);
		}
		// 缓存
		return cache_server;
	}

	private RemoteServer cache_server_local(String server_name) {
		LOGGER.warn("使用本地缓存的服务:".concat(server_name));
		RemoteServer native_server = depend_server_cache.get(server_name);
		if (native_server != null) {
			// 缓存服务到内存
			depend_server.put(server_name, native_server);
			// 加载成功事件处理
			server_loaded_handle_map.get(server_name).handle_event(native_server);
		}
		return native_server;
	}

	private RemoteServer cache_server(String server_name) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启-InfoGen.getInstance().start_and_watch(infogen_configuration);");
			return null;
		}
		try {
			String server_path = InfoGen_ZooKeeper.path(server_name);
			String server_data = ZK.get_data(server_path);
			if (server_data == null || server_data.trim().isEmpty()) {
				retry_cache_server_paths.add(server_name);
				LOGGER.error("服务节点数据为空:".concat(server_name));
				return null;
			}

			RemoteServer native_server = Tool_Jackson.toObject(server_data, RemoteServer.class);
			if (!native_server.available()) {
				retry_cache_server_paths.add(server_name);
				LOGGER.error("服务节点数据不可用:".concat(server_name));
				return null;
			}

			List<String> get_server_state = ZK.get_childrens_data(server_path);
			if (get_server_state.isEmpty()) {
				retry_cache_server_paths.add(server_name);
				LOGGER.error("服务子节点为空:".concat(server_name));
				return null;
			}

			for (String node_string : get_server_state) {
				try {
					RemoteNode node = Tool_Jackson.toObject(node_string, RemoteNode.class);
					native_server.add(node);
				} catch (Exception e) {
					LOGGER.error("转换节点数据错误:", e);
				}
			}

			// 缓存服务到内存
			depend_server.put(server_name, native_server);

			// 添加监听
			ZK.watcher_children_single(server_path, (path) -> {
				LOGGER.info("重新加载服务:".concat(path));
				reload_server(native_server);
			});

			// 加载成功事件处理
			server_loaded_handle_map.get(server_name).handle_event(native_server);

			// 持久化
			persistence();
			return native_server;
		} catch (Exception e) {
			retry_cache_server_paths.add(server_name);
			LOGGER.error("重新加载服务信息失败", e);
		}
		return null;
	}

	private void reload_server(RemoteServer native_server) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启-InfoGen.getInstance().start_and_watch(infogen_configuration);");
			return;
		}

		String server_name = native_server.getName();
		String server_path = InfoGen_ZooKeeper.path(server_name);

		List<String> remote_childrens = ZK.get_childrens(server_path);
		if (remote_childrens.isEmpty()) {
			LOGGER.error("服务子节点为空:".concat(server_name));
			return;
		}
		Map<String, RemoteNode> tmp_native_nodes = native_server.get_all_nodes();

		for (String node_path : remote_childrens) {
			RemoteNode node = tmp_native_nodes.get(node_path);
			// 本地和远端都存在该节点 - 从删除队列中去掉
			if (node != null) {
				tmp_native_nodes.remove(node_path);
				continue;
			}
			// 本地不存在远端存在该节点 - 加载到本地
			String node_string = ZK.get_data(server_path.concat("/").concat(node_path));
			try {
				native_server.add(Tool_Jackson.toObject(node_string, RemoteNode.class));
			} catch (Exception e) {
				LOGGER.error("节点数据错误:", e);
			}
		}
		// 本地存在远端不存在的节点 - 删除
		for (RemoteNode native_node : tmp_native_nodes.values()) {
			native_node.clean();
			native_server.remove(native_node);
		}

		// 缓存服务到内存
		depend_server.put(server_name, native_server);

		// 加载成功事件处理
		server_loaded_handle_map.get(server_name).handle_event(native_server);

		// 持久化
		persistence();

	}

	/////////////////////////////////////////////////////// 重试加载服务////////////////////////////////////////////////////////////////////////////
	private void retry_cache_server() {
		Set<String> tmp_reload_server_paths = new HashSet<>();
		tmp_reload_server_paths.addAll(retry_cache_server_paths);
		for (String server_name : tmp_reload_server_paths) {
			try {
				cache_server(server_name);
				retry_cache_server_paths.remove(server_name);
			} catch (Exception e) {
				LOGGER.error("重新加载服务失败:", e);
			}
		}
	}

	/////////////////////////////////////////////////////// 重新加载依赖的服务////////////////////////////////////////////////////////////////////
	private Boolean reload_all_server_flag = false;

	/**
	 * 重新加载所有服务
	 */
	public void reload_all_server() {
		reload_all_server_flag = true;
	}

	private void reload_all_server_delay() {
		if (reload_all_server_flag) {
			for (RemoteServer server : depend_server.values()) {
				reload_server(server);
			}
			reload_all_server_flag = false;
			LOGGER.info("重新加载所有服务成功");
		}
	}

	// ////////////////////////////////////////////////////持久化依赖的服务///////////////////////////////////////////////////////////////////////
	private Boolean persistence_flag = false;

	public void persistence() {
		persistence_flag = true;
	}

	private void persistence_delay() {
		try {
			if (persistence_flag) {
				Files.deleteIfExists(target_server_path);
				Files.move(source_server_path, target_server_path);
				Files.deleteIfExists(source_server_path);
				Files.write(source_server_path, Tool_Jackson.toJson(depend_server).getBytes(InfoGen_Configuration.charset));
				persistence_flag = false;
				LOGGER.info("持久化服务成功");
			}
		} catch (IOException e) {
			LOGGER.error("持久化服务失败", e);
		}
	}
}