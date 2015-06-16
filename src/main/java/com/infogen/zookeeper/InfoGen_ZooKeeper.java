/**
 * 
 */
package com.infogen.zookeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.infogen.util.Scheduled;
import com.infogen.zookeeper.event_handle.InfoGen_Zookeeper_Handle_Expired;
import com.infogen.zookeeper.event_handle.InfoGen_Zookeeper_Handle_Watcher_Children;
import com.infogen.zookeeper.event_handle.InfoGen_Zookeeper_Handle_Watcher_Data;

/**
 * zookeeper调用封装
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月24日 上午11:30:26
 */
public class InfoGen_ZooKeeper {
	public static final Logger logger = Logger.getLogger(InfoGen_ZooKeeper.class.getName());

	private static class InnerInstance {
		public static InfoGen_ZooKeeper instance = new InfoGen_ZooKeeper();
	}

	public static InfoGen_ZooKeeper getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_ZooKeeper() {
	}

	private String host_port;
	private ZooKeeper zookeeper;
	private InfoGen_Zookeeper_Handle_Expired expired_handle;
	public static String CONTEXT = "/infogen/";
	public static String CONTEXT_CONFIGURATION = "/infogen_configuration/";

	public static String path(String server_name) {
		return CONTEXT.concat(server_name);
	}

	public static String configuration_path(String configuration_name) {
		return CONTEXT_CONFIGURATION.concat(configuration_name);
	}

	public static String path(String server_name, String node_name) {
		return CONTEXT.concat(server_name).concat("/").concat(node_name);
	}

	/**
	 * 在服务启动时调用
	 * 
	 * @param host_port
	 * @throws IOException
	 */
	public void start_zookeeper(String host_port, InfoGen_Zookeeper_Handle_Expired expired_handle) throws IOException {
		if (zookeeper == null) {
			this.expired_handle = expired_handle;
			logger.info("启动zookeeper:".concat(host_port));
			this.host_port = host_port;
			this.zookeeper = new ZooKeeper(host_port, 10000, connect_watcher);

			reload_auth_info();
			logger.info("启动zookeeper成功:".concat(host_port));
		} else {
			logger.info("已经存在一个运行的zookeeper实例");
		}
	}

	private Map<String, Set<String>> map_auth_info = new HashMap<>();

	private void reload_auth_info() {
		for (Entry<String, Set<String>> entry : map_auth_info.entrySet()) {
			String scheme = entry.getKey();
			for (String auth : entry.getValue()) {
				zookeeper.addAuthInfo(scheme, auth.getBytes());
			}
		}
	}

	public void add_auth_info(String scheme, String auth) {
		Set<String> set_auth = map_auth_info.getOrDefault(scheme, new HashSet<String>());
		if (!set_auth.contains(auth)) {
			set_auth.add(auth);
			zookeeper.addAuthInfo(scheme, auth.getBytes());
		}
	}

	/**
	 * 只在重启zookeeper时调用
	 * 
	 * @throws InterruptedException
	 */
	public void stop_zookeeper() throws InterruptedException {
		logger.info("关闭zookeeper");
		zookeeper.close();
		zookeeper = null;
		logger.info("关闭zookeeper成功");
	}

	/**
	 * 只在服务启动时调用,所以采用同步调用,发生异常则退出程序检查
	 * 
	 * @param path
	 * @param data
	 * @return path
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public String create(String path, byte[] data, List<ACL> acls, CreateMode create_mode) {
		String _return = null;
		try {
			logger.info("创建节点:".concat(path));
			_return = zookeeper.create(path, data, acls, create_mode);
			logger.info("创建节点成功:".concat(_return));
		} catch (KeeperException e) {
			switch (e.code()) {
			case CONNECTIONLOSS:
				create(path, data, create_mode);
				logger.warn("连接中断,正在重试...: " + path);
				break;
			case NODEEXISTS:
				_return = Code.NODEEXISTS.name();
				logger.warn("节点已经存在: " + path);
				break;
			default:
				logger.error("未知错误: ", KeeperException.create(e.code(), path));
			}
		} catch (Exception e) {
			logger.error("未知程序中断错误: ", e);
		}
		return _return;
	}

	public String create(String path, byte[] data, CreateMode create_mode) {
		return create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, create_mode);
	}

	public Stat exists(String path) {
		Stat exists = null;
		try {
			logger.info("判断节点是否存在:".concat(path));
			exists = zookeeper.exists(path, false);
			logger.info("判断节点是否存在成功:".concat(path));
		} catch (Exception e) {
			logger.error("判断节点是否存在错误: ", e);
		}
		return exists;
	}

	public void delete(String path) {
		try {
			logger.info("删除节点:".concat(path));
			zookeeper.delete(path, -1);
			logger.info("删除节点成功:".concat(path));
		} catch (Exception e) {
			logger.error("删除节点错误: ", e);
		}
	}

	public String get_data(String path) {
		try {
			logger.info("获取节点数据:".concat(path));
			byte[] data = zookeeper.getData(path, false, null);
			if (data != null) {
				logger.info("获取节点数据成功:".concat(path));
				return new String(data);
			}
		} catch (Exception e) {
			logger.error("获取节点数据错误: ", e);
		}
		return null;
	}

	public Stat set_data(String path, byte[] data, int version) {
		try {
			logger.info("写入节点数据:".concat(path));
			Stat setData = zookeeper.setData(path, data, version);
			logger.info("写入节点数据成功:".concat(path));
			return setData;
		} catch (Exception e) {
			logger.error("写入节点数据失败: ", e);
		}
		return null;
	}

	public List<String> get_childrens(String path) {
		List<String> list = new ArrayList<String>();
		try {
			logger.info("获取子节点目录:".concat(path));
			list = zookeeper.getChildren(path, false).stream().filter(service_path -> !service_path.equals("zookeeper")).collect(Collectors.toList());
			logger.info("获取子节点目录成功:".concat(path));
		} catch (Exception e) {
			logger.error("获取子节点目录错误: ", e);
		}
		return list;
	}

	public List<String> get_childrens_data(String path) {
		List<String> list = new ArrayList<String>();
		try {
			logger.info("获取子节点数据:".concat(path));
			zookeeper.getChildren(path, false).stream().forEach(service_path -> {
				try {
					StringBuffer service_path_sbf = new StringBuffer(path);
					if (!path.equals("/")) {
						service_path_sbf.append("/");
					}
					service_path_sbf.append(service_path);
					byte[] data = zookeeper.getData(service_path_sbf.toString(), false, null);
					if (data != null) {
						list.add(new String(data));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			logger.info("获取子节点数据成功:".concat(path));
			return list;
		} catch (Exception e) {
			logger.error("获取子节点数据错误: ", e);
		}
		return null;
	}

	// //////////////////////////////////////////////节点数据 Watcher/////////////////////////////////////////////////////////////
	private Set<String> all_watcher_data_paths = new HashSet<String>();
	// 节点改变数据触发事件处理
	private Map<String, InfoGen_Zookeeper_Handle_Watcher_Data> watcher_data_handle_map = new HashMap<>();

	public void watcher_data_single(String path, InfoGen_Zookeeper_Handle_Watcher_Data watcher_data_handle) {
		if (watcher_data_handle_map.get(path) != null) {
			logger.info("当前监听已经注册过:".concat(path));
			return;
		}
		if (watcher_data_handle != null) {
			watcher_data_handle_map.put(path, watcher_data_handle);
		}
		all_watcher_data_paths.add(path);
		watcher_data(path);
	}

	private void watcher_data(String path) {
		try {
			logger.info("启动节点数据监听:".concat(path));
			zookeeper.getData(path, (event) -> {
				logger.info("节点数据事件  path:" + event.getPath() + "  state:" + event.getState().name() + "  type:" + event.getType().name());
				if (event.getType() == EventType.NodeDataChanged) {
					logger.info("重新加载节点信息:".concat(path));
					InfoGen_Zookeeper_Handle_Watcher_Data watcher_data_handle = watcher_data_handle_map.get(path);
					if (watcher_data_handle != null) {
						watcher_data_handle.handle_event(path);
					}
					watcher_data(path);
				} else if (event.getType() != EventType.None) {
					// EventType 为 None 的时候不需要重新监听
					logger.info("重新启动节点数据监听:".concat(path));
					watcher_data(path);
				}
			}, null);
			logger.info("启动节点数据监听成功:".concat(path));
		} catch (Exception e) {
			watcher_data_paths.add(path);
			logger.error("启动节点数据监听错误: ", e);
		}
	}

	// //////////////////////////////////////////////子节点 Watcher////////////////////////////////////////////////////////////////
	private Set<String> all_watcher_children_paths = new HashSet<String>();
	// 子节点改变触发事件处理
	private Map<String, InfoGen_Zookeeper_Handle_Watcher_Children> watcher_children_handle_map = new HashMap<>();

	// 创建子节点监听,如果已存在该字节点的监听直接返回
	public void watcher_children_single(String path, InfoGen_Zookeeper_Handle_Watcher_Children watcher_children_handle) {
		if (watcher_children_handle_map.get(path) != null) {
			logger.info("当前监听已经注册过:".concat(path));
			return;
		}
		if (watcher_children_handle != null) {
			watcher_children_handle_map.put(path, watcher_children_handle);
		}
		all_watcher_children_paths.add(path);
		watcher_children(path);
	}

	private void watcher_children(String path) {
		try {
			logger.info("启动子节点监听:".concat(path));
			zookeeper.getChildren(path, (event) -> {
				logger.info("子节点事件  path:" + event.getPath() + "  state:" + event.getState().name() + "  type:" + event.getType().name());
				if (event.getType() == EventType.NodeChildrenChanged) {
					logger.info("重新加载服务信息:".concat(path));
					InfoGen_Zookeeper_Handle_Watcher_Children watcher_children_handle = watcher_children_handle_map.get(path);
					if (watcher_children_handle != null) {
						watcher_children_handle.handle_event(path);
					}
					watcher_children(path);
				} else if (event.getType() != EventType.None) {
					logger.info("重新启动子节点监听:".concat(path));
					watcher_children(path);
				}
				// EventType 为 None 的时候不需要重新监听
				});
			logger.info("启动子节点监听成功:".concat(path));
		} catch (Exception e) {
			watcher_children_paths.add(path);
			logger.error("启动子节点监听错误: ", e);
		}
		System.out.println();
	}

	// ///////////////////////////////////////连接 Watcher///////////////////////////////////////////////////

	/**
	 * 只对 Client 的连接状态变化做出反应
	 */
	private Watcher connect_watcher = new Watcher() {
		@Override
		public void process(WatchedEvent event) {
			logger.info("连接事件  path:" + event.getPath() + "  state:" + event.getState().name() + "  type:" + event.getType().name());
			if (event.getType() == Watcher.Event.EventType.None) {
				switch (event.getState()) {
				case SyncConnected:
					break;
				case Expired:
					try {
						logger.error("zookeeper 连接过期");

						logger.info("重启zookeeper");
						stop_zookeeper();
						start_zookeeper(host_port, expired_handle);
						logger.info("重置所有子节点监听");
						watcher_children_paths.addAll(all_watcher_children_paths);
						logger.info("重置所有节点数据监听");
						watcher_data_paths.addAll(all_watcher_data_paths);
						logger.info("其它定制处理");
						if (expired_handle != null) {
							expired_handle.handle_event();
						}
					} catch (Exception e) {
						logger.error("zookeeper 重连错误");
					}
					break;
				case Disconnected:
					break;
				default:
					break;
				}
			}
		}
	};

	// //////////////////////////////////////////GETTER SETTER/////////////////////////////////////////////////////
	public String getHost_port() {
		return host_port;
	}

	public void setHost_port(String host_port) {
		this.host_port = host_port;
	}

	// ////////////////////////////////////////////Scheduled////////////////////////////////////////////////////////////////
	// 定时重试监听失败的服务
	private Set<String> watcher_children_paths = new HashSet<String>();
	private Set<String> watcher_data_paths = new HashSet<String>();

	public void schedule() {
		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			Set<String> paths = new HashSet<String>();
			paths.addAll(watcher_children_paths);
			watcher_children_paths.clear();

			paths.forEach(server_name -> {
				try {
					watcher_children(server_name);
				} catch (Exception e) {
					logger.error("重新执行子节点监听", e);
				}
			});
		}, 30, 30, TimeUnit.SECONDS);

		Scheduled.executors_single.scheduleWithFixedDelay(() -> {
			Set<String> paths = new HashSet<String>();
			paths.addAll(watcher_data_paths);
			watcher_data_paths.clear();

			paths.forEach(configuration_name -> {
				try {
					watcher_data(configuration_name);
				} catch (Exception e) {
					logger.error("重新执行节点数据监听", e);
				}
			});
		}, 30, 30, TimeUnit.SECONDS);
	}

}
