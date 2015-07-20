package com.infogen.server.cache;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.server.model.NativeNode;
import com.infogen.server.model.NativeServer;
import com.infogen.server.zookeeper.InfoGen_ZooKeeper;
import com.infogen.server.zookeeper.InfoGen_Zookeeper_Handle_Expired;
import com.infogen.util.Scheduled;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.tools.Tool_Jackson;
import com.larrylgq.aop.util.NativePath;

/**
 * 加载和缓存服务及配置数据
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月1日 下午4:37:30
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
		// 定时重新加载失败的服务
		Scheduled.executors_single.scheduleWithFixedDelay(reload_server_runnable, 30, 30, TimeUnit.SECONDS);
		Scheduled.executors_single.scheduleWithFixedDelay(persistence_runnable, 30, 30, TimeUnit.SECONDS);
	}

	private InfoGen_ZooKeeper ZK = com.infogen.server.zookeeper.InfoGen_ZooKeeper.getInstance();

	// 依赖的服务
	public final ConcurrentMap<String, NativeServer> depend_server = new ConcurrentHashMap<>();
	// 本地缓存的依赖服务
	private ConcurrentMap<String, NativeServer> depend_server_cache = new ConcurrentHashMap<>();

	// ///////////////////////////////////////////////////初始化///////////////////////////////////////////////
	private Path source_server_path = NativePath.get("infogen.cache.server.js");
	private Path target_server_path = NativePath.get("infogen.cache.server.js.copy");

	public void init(InfoGen_Configuration infogen_configuration, InfoGen_Zookeeper_Handle_Expired expired_handle) throws IOException, URISyntaxException {
		// 初始化 zookeeper
		ZK.start_zookeeper(infogen_configuration.zookeeper, expired_handle);

		// 初始化所有需要的配置文件 如果不存在则创建
		Tool_Core.prepare_files(source_server_path, target_server_path);

		// 获取缓存的服务
		String server_json = Tool_Core.load_file(source_server_path);
		if (!server_json.isEmpty()) {
			ConcurrentHashMap<String, NativeServer> fromJson = Tool_Jackson.toObject(server_json, new TypeReference<ConcurrentHashMap<String, NativeServer>>() {
			});
			depend_server_cache = fromJson;
		}
	}

	// ////////////////////////////////////////////cache_server////////////////////////////////////////////////////////////////
	// 注册的cache完成事件处理器
	private Map<String, InfoGen_Loaded_Handle_Server> server_loaded_handle_map = new HashMap<>();
	private Set<String> reload_server_paths = new HashSet<>();

	public NativeServer cache_server_single(String server_name, InfoGen_Loaded_Handle_Server server_loaded_handle) {
		if (server_loaded_handle_map.get(server_name) != null) {
			LOGGER.warn("当前缓存过该服务:".concat(server_name));
			return depend_server.get(server_name);
		}

		// 注册 server 加载完成的事件
		if (server_loaded_handle == null) {
			server_loaded_handle = (native_server) -> {
			};
		}
		server_loaded_handle_map.put(server_name, server_loaded_handle);

		NativeServer cache_server = cache_server(server_name);
		// 获取不到,到本地缓存里查找
		if (cache_server == null) {
			cache_server = depend_server_cache.get(server_name);
			if (cache_server != null) {
				depend_server.put(server_name, cache_server);
				server_loaded_handle.handle_event(cache_server);
				LOGGER.warn("使用本地缓存的服务:".concat(server_name));
			}
		}
		return cache_server;
	}

	/**
	 * 重新加载所有服务
	 */
	public void reload_all_server() {
		for (NativeServer server : depend_server.values()) {
			reload_server(server);
		}
	}

	private NativeServer cache_server(String server_name) {
		try {
			String server_path = InfoGen_ZooKeeper.path(server_name);
			String server_data = ZK.get_data(server_path);
			if (server_data == null || server_data.trim().isEmpty()) {
				reload_server_paths.add(server_name);
				LOGGER.error("服务节点数据为空:".concat(server_name));
				return null;
			}

			NativeServer native_server = Tool_Jackson.toObject(server_data, NativeServer.class);
			if (!native_server.available()) {
				reload_server_paths.add(server_name);
				LOGGER.error("服务节点数据不可用:".concat(server_name));
				return null;
			}

			List<String> get_server_state = ZK.get_childrens_data(server_path);
			if (get_server_state.isEmpty()) {
				reload_server_paths.add(server_name);
				LOGGER.error("服务子节点为空:".concat(server_name));
				return null;
			}

			for (String node_string : get_server_state) {
				try {
					NativeNode node = Tool_Jackson.toObject(node_string, NativeNode.class);
					native_server.add(node);
				} catch (Exception e) {
					LOGGER.error("转换节点数据错误:", e);
				}
			}

			// 添加监听
			ZK.watcher_children_single(InfoGen_ZooKeeper.path(server_name), (path) -> {
				reload_server(native_server);
			});

			// 缓存
			depend_server.put(server_name, native_server);

			// 加载成功事件处理
			InfoGen_Loaded_Handle_Server server_loaded_handle = server_loaded_handle_map.get(server_name);
			if (server_loaded_handle != null) {
				server_loaded_handle.handle_event(native_server);
			}

			// 持久化
			persistence();
			return native_server;
		} catch (Exception e) {
			reload_server_paths.add(server_name);
			LOGGER.error("重新加载服务信息失败", e);
		}
		return null;
	}

	private void reload_server(NativeServer native_server) {
		String server_name = native_server.getName();
		String server_path = InfoGen_ZooKeeper.path(server_name);
		List<String> get_childrens = ZK.get_childrens(server_path);
		if (get_childrens.isEmpty()) {
			return;
		}
		Map<String, NativeNode> tmp_all_nodes = native_server.get_all_nodes();
		for (String node_path : get_childrens) {
			NativeNode node = tmp_all_nodes.get(node_path);
			// 本地存在该节点 - 继续
			if (node != null) {
				tmp_all_nodes.remove(node_path);
				continue;
			}
			// 本地不存在该节点 - 添加到本地
			String node_string = ZK.get_data(server_path.concat("/").concat(node_path));
			try {
				native_server.add(Tool_Jackson.toObject(node_string, NativeNode.class));
			} catch (Exception e) {
				LOGGER.error("节点数据错误:", e);
			}
		}
		// 注册中心不存在的节点 - 删除
		for (NativeNode native_node : tmp_all_nodes.values()) {
			native_node.clean();
			native_server.remove(native_node);
		}

		// 加载成功事件处理
		InfoGen_Loaded_Handle_Server server_loaded_handle = server_loaded_handle_map.get(server_name);
		if (server_loaded_handle != null) {
			server_loaded_handle.handle_event(native_server);
		}

		// 持久化
		persistence();
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

	// ////////////////////////////////////////////Scheduled////////////////////////////////////////////////////////////////
	private final Runnable reload_server_runnable = new Runnable() {
		@Override
		public void run() {
			Set<String> tmp_reload_server_paths = new HashSet<>();
			tmp_reload_server_paths.addAll(reload_server_paths);
			for (String server_name : tmp_reload_server_paths) {
				try {
					cache_server(server_name);
					reload_server_paths.remove(server_name);
				} catch (Exception e) {
					LOGGER.error("重新执行上次加载失败的服务", e);
				}
			}
		}
	};

	private final Runnable persistence_runnable = new Runnable() {
		@Override
		public void run() {
			persistence_delay();
		}
	};
}
