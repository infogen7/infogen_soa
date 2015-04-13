/**
 * 
 */
package com.infogen.cache;

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
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.event_handle.Server_Loaded_Handle;
import com.infogen.server.NativeNode;
import com.infogen.server.NativeServer;
import com.infogen.tools.Tool_Core;
import com.infogen.tools.Tool_Jackson;
import com.infogen.util.NativePath;
import com.infogen.util.Scheduled;
import com.infogen.zookeeper.InfoGen_ZooKeeper;

/**
 * 加载和缓存服务及配置数据
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月1日 下午4:37:30
 */
public class InfoGen_Server_Cache {
	public static final Logger logger = Logger.getLogger(InfoGen_Server_Cache.class.getName());

	private static class InnerInstance {
		public static InfoGen_Server_Cache instance = new InfoGen_Server_Cache();
	}

	public static InfoGen_Server_Cache getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Server_Cache() {
	}

	private InfoGen_ZooKeeper ZK = com.infogen.zookeeper.InfoGen_ZooKeeper.getInstance();

	// 依赖的服务
	public ConcurrentHashMap<String, NativeServer> depend_server = new ConcurrentHashMap<>();
	// 本地缓存的依赖服务
	public ConcurrentHashMap<String, NativeServer> depend_server_cache = new ConcurrentHashMap<>();

	// ////////////////////////////////////////////cache_server////////////////////////////////////////////////////////////////
	// 注册的cache完成事件处理器
	private Map<String, Server_Loaded_Handle> server_loaded_handle_map = new HashMap<>();
	private Set<String> reload_server_paths = new HashSet<>();

	// 未考虑并发
	public NativeServer cache_server_single(String server_name, Server_Loaded_Handle server_loaded_handle) {
		if (server_loaded_handle_map.get(server_name) != null) {
			logger.warn("当前缓存过该服务:".concat(server_name));
			return depend_server.get(server_name);
		}

		if (server_loaded_handle != null) {
			server_loaded_handle_map.put(server_name, server_loaded_handle);
		}
		return cache_server(server_name);
	}

	private NativeServer cache_server(String server_name) {
		try {
			String get_data = ZK.get_data(InfoGen_ZooKeeper.path(server_name));
			if (get_data == null || get_data.trim().isEmpty()) {
				logger.error("服务节点数据为空:".concat(server_name));
				reload_server_paths.add(server_name);
				return null;
			}
			NativeServer server = Tool_Jackson.toObject(get_data, NativeServer.class);
			if (!server.available()) {
				reload_server_paths.add(server_name);
				return null;
			}
			List<String> get_server_state = ZK.get_childrens_data(InfoGen_ZooKeeper.path(server_name));
			if (get_server_state == null) {
				reload_server_paths.add(server_name);
				return null;
			}

			for (String node_string : get_server_state) {
				try {
					NativeNode node = Tool_Jackson.toObject(node_string, NativeNode.class);
					if (node.available()) {
						server.getAvailable_nodes().add(node);
					}
				} catch (Exception e) {
					logger.error("节点数据错误:", e);
				}
			}
			// 事件处理
			Server_Loaded_Handle server_loaded_handle = server_loaded_handle_map.get(server_name);
			if (server_loaded_handle != null) {
				server_loaded_handle.handle_event(server);
			}
			// 添加监听
			ZK.watcher_children_single(InfoGen_ZooKeeper.path(server_name), (path) -> {
				cache_server(path.replace(InfoGen_ZooKeeper.CONTEXT, ""));
			});
			return server;
		} catch (Exception e) {
			reload_server_paths.add(server_name);
			logger.error("重新加载服务信息失败", e);
		}
		return null;
	}

	// ///////////////////////////////////////////////////读取本地缓存的依赖服务配置///////////////////////////////////////////////
	private Path source_server_path = NativePath.get("infogen.cache.server.js");
	private Path target_server_path = NativePath.get("infogen.cache.server.js.copy");

	public void init_local_infogen_cfg_dynamic() throws IOException, URISyntaxException {
		// 初始化所有需要的配置文件
		Tool_Core.prepare_files(source_server_path, target_server_path);

		// 获取缓存的服务
		String server_json = Tool_Core.load_file(source_server_path);
		if (!server_json.isEmpty()) {
			ConcurrentHashMap<String, NativeServer> fromJson = Tool_Jackson.toObject(server_json, new TypeReference<ConcurrentHashMap<String, NativeServer>>() {
			});
			depend_server_cache = fromJson;
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
				logger.info("持久化服务成功");
			}
		} catch (IOException e) {
			logger.error("持久化服务失败", e);
		}
	}

	// ////////////////////////////////////////////Scheduled////////////////////////////////////////////////////////////////
	public void schedule() {
		Scheduled.executors.scheduleWithFixedDelay(() -> {
			Set<String> tmp_reload_server_paths = new HashSet<>();
			tmp_reload_server_paths.addAll(reload_server_paths);
			reload_server_paths.clear();

			tmp_reload_server_paths.forEach(server_name -> {
				try {
					cache_server(server_name);
				} catch (Exception e) {
					logger.error("重新执行上次加载失败的服务", e);
				}
			});
		}, 30, 30, TimeUnit.SECONDS);

		Scheduled.executors.scheduleWithFixedDelay(() -> {
			persistence_delay();
		}, 30, 30, TimeUnit.SECONDS);
	}
}
