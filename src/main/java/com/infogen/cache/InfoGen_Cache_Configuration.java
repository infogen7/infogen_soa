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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.infogen.cache.event_handle.InfoGen_Loaded_Handle_Configuration;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.util.Scheduled;
import com.infogen.zookeeper.InfoGen_ZooKeeper;
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
public class InfoGen_Cache_Configuration {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Cache_Configuration.class.getName());

	private static class InnerInstance {
		public static InfoGen_Cache_Configuration instance = new InfoGen_Cache_Configuration();
	}

	public static InfoGen_Cache_Configuration getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Cache_Configuration() {
	}

	private InfoGen_ZooKeeper ZK = com.infogen.zookeeper.InfoGen_ZooKeeper.getInstance();

	// 依赖的配置
	public ConcurrentHashMap<String, String> depend_configuration = new ConcurrentHashMap<>();
	// 本地缓存的依赖配置
	public ConcurrentHashMap<String, String> depend_configuration_cache = new ConcurrentHashMap<>();

	// ////////////////////////////////////////////cache_configuration////////////////////////////////////////////////////////////////
	// 注册的cache完成事件处理器
	private Map<String, InfoGen_Loaded_Handle_Configuration> configuration_loaded_handle_map = new HashMap<>();
	private Set<String> reload_configuration_paths = new HashSet<>();

	// 未考虑并发
	public String cache_configuration_single(String configuration_name, InfoGen_Loaded_Handle_Configuration configuration_loaded_handle) {
		if (configuration_loaded_handle_map.get(configuration_name) != null) {
			LOGGER.warn("当前缓存过该配置:".concat(configuration_name));
			return depend_configuration.get(configuration_name);
		}

		if (configuration_loaded_handle != null) {
			configuration_loaded_handle_map.put(configuration_name, configuration_loaded_handle);
		}
		return cache_configuration(configuration_name);
	}

	private String cache_configuration(String configuration_name) {
		try {
			String get_data = ZK.get_data(InfoGen_ZooKeeper.configuration_path(configuration_name));
			if (get_data == null) {
				reload_configuration_paths.add(configuration_name);
				return null;
			}
			// 事件处理
			InfoGen_Loaded_Handle_Configuration configuration_loaded_handle = configuration_loaded_handle_map.get(configuration_name);
			if (configuration_loaded_handle != null) {
				configuration_loaded_handle.handle_event(get_data);
			}
			// 添加监听
			ZK.watcher_data_single(InfoGen_ZooKeeper.configuration_path(configuration_name), (path) -> {
				cache_configuration(path.replace(InfoGen_ZooKeeper.CONTEXT_CONFIGURATION, ""));
			});
			return get_data;
		} catch (Exception e) {
			reload_configuration_paths.add(configuration_name);
			LOGGER.error("重新加载配置信息失败", e);
		}
		return null;
	}

	// ///////////////////////////////////////////////////读取本地缓存的依赖服务配置///////////////////////////////////////////////
	private Path source_configuration_path = NativePath.get("infogen.cache.configuration.js");
	private Path target_configuration_path = NativePath.get("infogen.cache.configuration.js.copy");

	public void init_local_infogen_cfg_dynamic() throws IOException, URISyntaxException {
		// 初始化所有需要的配置文件
		Tool_Core.prepare_files(source_configuration_path, target_configuration_path);
		// 获取缓存的配置
		String configuration_json = Tool_Core.load_file(source_configuration_path);
		if (!configuration_json.isEmpty()) {
			ConcurrentHashMap<String, String> fromJson = Tool_Jackson.toObject(configuration_json, new TypeReference<ConcurrentHashMap<String, String>>() {
			});
			depend_configuration_cache = fromJson;
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
				Files.deleteIfExists(target_configuration_path);
				Files.move(source_configuration_path, target_configuration_path);
				Files.deleteIfExists(source_configuration_path);
				Files.write(source_configuration_path, Tool_Jackson.toJson(depend_configuration).getBytes(InfoGen_Configuration.charset));
				persistence_flag = false;
				LOGGER.info("持久化配置成功");
			}
		} catch (IOException e) {
			LOGGER.error("持久化配置失败", e);
		}
	}

	// ////////////////////////////////////////////Scheduled////////////////////////////////////////////////////////////////
	public void schedule() {

		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			Set<String> tmp_reload_configuration_paths = new HashSet<>();
			tmp_reload_configuration_paths.addAll(reload_configuration_paths);
			reload_configuration_paths.clear();

			tmp_reload_configuration_paths.forEach(configuration_name -> {
				try {
					cache_configuration(configuration_name);
				} catch (Exception e) {
					LOGGER.error("重新执行上次加载失败的配置", e);
				}
			});
		}, 30, 30, TimeUnit.SECONDS);

		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			persistence_delay();
		}, 30, 30, TimeUnit.SECONDS);
	}
}
