package com.infogen.server.management;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.json.Jackson;
import com.infogen.core.path.NativePath;
import com.infogen.server.model.RegisterNode;
import com.infogen.server.model.RegisterServer;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.infogen.server.model.ServiceFunctions;
import com.infogen.server.zookeeper.InfoGen_ZooKeeper;
import com.infogen.server.zookeeper.InfoGen_Zookeeper_Handle_Expired;
import com.infogen.tools.Scheduled;

/**
 * 加载和缓存服务
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:25:01
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Server_Management {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Server_Management.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Server_Management instance = new InfoGen_Server_Management();
	}

	public static InfoGen_Server_Management getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Server_Management() {
		// Scheduled
		// 定时重新加载获取失败的服务
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			retry_cache_server();
		}, 30, 30, TimeUnit.SECONDS);
		// 定时检测并持久化服务到本地
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			persistence_delay();
		}, 30, 30, TimeUnit.SECONDS);
		// 定时检测并重新加载所有依赖的服务
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			reload_all_server_delay();
		}, 3, 3, TimeUnit.MINUTES);
	}

	private InfoGen_ZooKeeper ZK = com.infogen.server.zookeeper.InfoGen_ZooKeeper.getInstance();
	private Path source_server_path = NativePath.get("infogen.cache.server.js");
	private Path target_server_path = NativePath.get("infogen.cache.server.js.copy");

	// 依赖的服务
	public final ConcurrentMap<String, RemoteServer> depend_server = new ConcurrentHashMap<>();
	// 本地缓存的依赖服务
	public final ConcurrentMap<String, RemoteServer> depend_server_cache = new ConcurrentHashMap<>();
	// 需要重试加载的服务
	private Set<String> retry_cache_server_paths = new HashSet<>();
	// 加载完成后触发的事件
	private Map<String, InfoGen_Loaded_Handle_Server> server_loaded_handle_map = new HashMap<>();

	// ///////////////////////////////////////////////////初始化///////////////////////////////////////////////

	public void init(InfoGen_Configuration infogen_configuration, InfoGen_Zookeeper_Handle_Expired expired_handle) throws IOException, URISyntaxException {
		// 初始化 zookeeper
		ZK.start_zookeeper(infogen_configuration.zookeeper, expired_handle);
		ZK.create_notexists(InfoGen_ZooKeeper.CONTEXT, CreateMode.PERSISTENT);
		ZK.create_notexists(InfoGen_ZooKeeper.CONTEXT_FUNCTIONS, CreateMode.PERSISTENT);

		// 初始化所有需要的配置文件 如果不存在则创建
		com.infogen.core.tools.Files.prepare_files(source_server_path, target_server_path);

		// 获取缓存的服务
		String server_json = com.infogen.core.tools.Files.load_file(source_server_path);
		if (!server_json.isEmpty()) {
			ConcurrentHashMap<String, RemoteServer> fromJson = Jackson.toObject(server_json, new TypeReference<ConcurrentHashMap<String, RemoteServer>>() {
			});
			for (String key : fromJson.keySet()) {
				depend_server_cache.put(key, fromJson.get(key));
			}
		}
	}

	//////////////////////////////////////////////////////// create_server/////////////////////////////////////////////////////////
	public void create_service_functions(ServiceFunctions service_functions) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启- InfoGen.create('infogen.properties').start();");
			return;
		}

		String path = InfoGen_ZooKeeper.functions_path(service_functions.getServer().getName());
		// 创建或更新服务节点
		byte[] bytes = Jackson.toJson(service_functions).getBytes();
		String create_path = ZK.create(path, bytes, CreateMode.PERSISTENT);
		if (create_path == null) {
			LOGGER.error("注册自身服务方法列表失败!");
			return;
		} else if (create_path.equals(Code.NODEEXISTS.name())) {
			// 更新服务节点数据
			ZK.set_data(path, bytes, -1);
		}
	}

	// 生成一个服务节点
	public void create_server(RegisterServer register_server, Boolean update) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启- InfoGen.create('infogen.properties').start();");
			return;
		}

		String path = InfoGen_ZooKeeper.path(register_server.getName());
		// 创建或更新服务节点
		byte[] bytes = Jackson.toJson(register_server).getBytes();
		String create_path = ZK.create(path, bytes, CreateMode.PERSISTENT);
		if (create_path == null) {
			LOGGER.error("注册自身服务失败!");
			return;
		} else if (create_path.equals(Code.NODEEXISTS.name())) {
			// 更新服务节点数据
			if (update) {
				ZK.set_data(path, bytes, -1);
			}
		}
	}

	// 生成一个服务实例节点
	public void create_node(RegisterNode register_node) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启- InfoGen.create('infogen.properties').start();");
			return;
		}

		// 创建应用子节点及子节点数据
		String path = InfoGen_ZooKeeper.path(register_node.getServer_name(), register_node.getName());
		String create_path = ZK.create(path, Jackson.toJson(register_node).getBytes(), CreateMode.EPHEMERAL);
		if (create_path == null) {
			LOGGER.error("#注册自身节点失败!");
			return;
		}
	}

	// ////////////////////////////////////////////cache_server////////////////////////////////////////////////////////////////
	// 注册的cache完成事件处理器
	public RemoteServer cache_server_single(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		if (server_loaded_handle_map.get(server_name) != null) {
			LOGGER.warn("#已经初始化过该服务:".concat(server_name));
			return depend_server.get(server_name);
		}

		if (server_loaded_handle == null) {
			server_loaded_handle = (native_server) -> {
			};
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
			LOGGER.warn("InfoGen服务没有开启- InfoGen.create('infogen.properties').start();");
			return null;
		}
		String server_path = InfoGen_ZooKeeper.path(server_name);
		try {
			String server_data = ZK.get_data(server_path);

			if (server_data == null || server_data.trim().isEmpty()) {
				retry_cache_server_paths.add(server_name);
				LOGGER.error("服务节点数据为空:".concat(server_name));
				return null;
			}

			RemoteServer native_server = Jackson.toObject(server_data, RemoteServer.class);
			if (!native_server.available()) {
				retry_cache_server_paths.add(server_name);
				LOGGER.error("服务节点数据不可用:".concat(server_name));
				return null;
			}

			for (String node_name : ZK.get_childrens(server_path)) {
				String node_string = ZK.get_data(InfoGen_ZooKeeper.path(server_name, node_name));
				try {
					RemoteNode node = Jackson.toObject(node_string, RemoteNode.class);
					native_server.add(node);
				} catch (Exception e) {
					LOGGER.error("转换节点数据错误:", e);
				}
			}

			// 添加监听
			ZK.watcher_children_single(server_path, (path) -> {
				LOGGER.info("重新加载服务:".concat(path));
				reload_server(native_server);
			});

			// 缓存服务到内存
			// 加载成功事件处理
			// 持久化
			depend_server.put(server_name, native_server);
			server_loaded_handle_map.get(server_name).handle_event(native_server);
			persistence_flag = true;
			return native_server;
		} catch (Exception e) {
			retry_cache_server_paths.add(server_name);
			LOGGER.error("加载服务信息失败", e);
		}
		return null;
	}

	private void reload_server(RemoteServer native_server) {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启- InfoGen.create('infogen.properties').start();");
			return;
		}

		String server_name = native_server.getName();
		String server_path = InfoGen_ZooKeeper.path(server_name);

		Map<String, RemoteNode> delete_native_nodes = native_server.get_all_nodes();

		for (String node_name : ZK.get_childrens(server_path)) {
			RemoteNode node = delete_native_nodes.get(node_name);
			// 本地和远端都存在该节点 - 从删除队列中去掉
			if (node != null) {
				delete_native_nodes.remove(node_name);
				continue;
			}
			// 本地不存在远端存在该节点 - 加载到本地
			String node_string = ZK.get_data(InfoGen_ZooKeeper.path(server_name, node_name));
			try {
				native_server.add(Jackson.toObject(node_string, RemoteNode.class));
			} catch (Exception e) {
				LOGGER.error("转换节点数据错误:", e);
			}
		}
		// 本地存在远端不存在的节点 - 删除
		for (RemoteNode native_node : delete_native_nodes.values()) {
			native_server.remove(native_node);
		}

		// 缓存服务到内存
		// 加载成功事件处理
		// 持久化
		depend_server.put(server_name, native_server);
		server_loaded_handle_map.get(server_name).handle_event(native_server);
		persistence_flag = true;
	}

	/////////////////////////////////////////////////////// 重试加载服务////////////////////////////////////////////////////////////////////////////
	private void retry_cache_server() {
		Set<String> tmp_reload_server_paths = new HashSet<>();
		tmp_reload_server_paths.addAll(retry_cache_server_paths);
		for (String server_name : tmp_reload_server_paths) {
			try {
				/** 一定要先remove再cache 因为cache失败会将server_name添加到队列 **/
				retry_cache_server_paths.remove(server_name);
				cache_server(server_name);
			} catch (Exception e) {
				LOGGER.error("重新加载服务失败:", e);
			}
		}
	}

	/////////////////////////////////////////////////////// 重新加载依赖的服务////////////////////////////////////////////////////////////////////
	public Boolean reload_all_server_flag = false;

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
	private final static Charset charset = StandardCharsets.UTF_8;
	public Boolean persistence_flag = false;

	private void persistence_delay() {
		try {
			if (persistence_flag) {
				/** 防止正在持久化的时候服务挂掉导致的持久化数据丢失 **/
				Files.deleteIfExists(target_server_path);
				Files.move(source_server_path, target_server_path);
				Files.deleteIfExists(source_server_path);
				Files.write(source_server_path, Jackson.toJson(depend_server).getBytes(charset));
				persistence_flag = false;
				LOGGER.info("持久化服务成功");
			}
		} catch (IOException e) {
			LOGGER.error("持久化服务失败", e);
		}
	}

	///////////////////////////////////////////////////////////// 代码备份 ////////////////////////////////////////////////////////////////////////////////////
	// 写入或更新一个节点数据
	@SuppressWarnings("unused")
	private void upsert_data(String name, String value, String digest) throws NoSuchAlgorithmException {
		if (!ZK.available()) {
			LOGGER.warn("InfoGen服务没有开启- InfoGen.create('infogen.properties').start();");
			return;
		}

		List<ACL> acls = new ArrayList<ACL>();
		// 采用用户名密码形式
		acls.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(digest))));
		// 所有用户可读权限
		acls.add(new ACL(ZooDefs.Perms.READ, new Id("world", "anyone")));
		// 创建或更新配置节点
		String create_path = ZK.create(InfoGen_ZooKeeper.path(name), value.getBytes(), acls, CreateMode.PERSISTENT);
		if (create_path == null) {
			LOGGER.error("注册配置失败");
		} else if (create_path.equals(Code.NODEEXISTS.name())) {
			ZK.add_auth_info("digest", digest);
			ZK.set_data(InfoGen_ZooKeeper.path(name), value.getBytes(), -1);
		}
	}
	///////////////////////////////////////////////////////////// END 代码备份 //////////////////////////////////////////////////////////////////////////
}