package com.infogen.server.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.infogen.server.cache.zookeeper.InfoGen_Zookeeper_Handle_Expired;
import com.infogen.server.cache.zookeeper.InfoGen_Zookeeper_Handle_Watcher_Children;
import com.infogen.tools.Scheduled;

/**
 * zookeeper调用封装
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:30:44
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_ZooKeeper {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_ZooKeeper.class.getName());

	private static class InnerInstance {
		public static final InfoGen_ZooKeeper instance = new InfoGen_ZooKeeper();
	}

	public static InfoGen_ZooKeeper getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_ZooKeeper() {
	}

	private String host_port;
	private ZooKeeper zookeeper;
	private InfoGen_Zookeeper_Handle_Expired expired_handle;
	private Map<String, Set<String>> map_auth_info = new HashMap<>();
	private static final String CONTEXT = "/infogen";
	private static final String CONTEXT_FUNCTIONS = "/infogen_functions";

	protected static String functions_path(String server_name) {
		return CONTEXT_FUNCTIONS.concat("/").concat(server_name);
	}

	protected static String path() {
		return CONTEXT;
	}

	protected static String path(String server_name) {
		return CONTEXT.concat("/").concat(server_name);
	}

	protected static String path(String server_name, String node_name) {
		return CONTEXT.concat("/").concat(server_name).concat("/").concat(node_name);
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
			LOGGER.info("启动zookeeper:".concat(host_port));
			this.host_port = host_port;
			this.zookeeper = new ZooKeeper(host_port, 10000, connect_watcher);

			for (Entry<String, Set<String>> entry : map_auth_info.entrySet()) {
				String scheme = entry.getKey();
				for (String auth : entry.getValue()) {
					zookeeper.addAuthInfo(scheme, auth.getBytes());
				}
			}
			LOGGER.info("启动zookeeper成功:".concat(host_port));
		} else {
			LOGGER.info("已经存在一个运行的zookeeper实例");
		}
	}

	/**
	 * 只在重启zookeeper时调用
	 * 
	 * @throws InterruptedException
	 */
	public void stop_zookeeper() throws InterruptedException {
		LOGGER.info("关闭zookeeper");
		zookeeper.close();
		zookeeper = null;
		LOGGER.info("关闭zookeeper成功");
	}

	public Boolean available() {
		return (zookeeper != null);
	}

	////////////////////////////////////////////////// 安全认证//////////////////////////////////
	public void add_auth_info(String scheme, String auth) {
		Set<String> set_auth = map_auth_info.getOrDefault(scheme, new HashSet<String>());
		if (!set_auth.contains(auth)) {
			set_auth.add(auth);
			zookeeper.addAuthInfo(scheme, auth.getBytes());
		}
	}

	//////////////////////////////////////////////// 节点操作/////////////////////////////////////////////////
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
			LOGGER.info("创建节点:".concat(path));
			_return = zookeeper.create(path, data, acls, create_mode);
			LOGGER.info("创建节点成功:".concat(_return));
		} catch (KeeperException e) {
			switch (e.code()) {
			case CONNECTIONLOSS:
				LOGGER.warn("连接中断,正在重试创建节点...: " + path);
				create(path, data, create_mode);
				break;
			case NODEEXISTS:
				LOGGER.warn("节点已经存在: " + path);
				_return = Code.NODEEXISTS.name();
				break;
			default:
				LOGGER.error("未知错误: ", KeeperException.create(e.code(), path));
			}
		} catch (Exception e) {
			LOGGER.error("未知程序中断错误: ", e);
		}
		return _return;
	}

	public String create(String path, byte[] data, CreateMode create_mode) {
		return create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, create_mode);
	}

	public Stat exists(String path) {
		Stat exists = null;
		try {
			LOGGER.info("判断节点是否存在:".concat(path));
			exists = zookeeper.exists(path, false);
			LOGGER.info("判断节点是否存在成功:".concat(path));
		} catch (Exception e) {
			LOGGER.error("判断节点是否存在错误: ", e);
		}
		return exists;
	}

	public void delete(String path) {
		try {
			LOGGER.info("删除节点:".concat(path));
			zookeeper.delete(path, -1);
			LOGGER.info("删除节点成功:".concat(path));
		} catch (Exception e) {
			LOGGER.error("删除节点错误: ", e);
		}
	}

	public String get_data(String path) {
		try {
			LOGGER.info("获取节点数据:".concat(path));
			byte[] data = zookeeper.getData(path, false, null);
			if (data != null) {
				LOGGER.info("获取节点数据成功:".concat(path));
				return new String(data);
			}
		} catch (Exception e) {
			LOGGER.error("获取节点数据错误: ", e);
		}
		return null;
	}

	public Stat set_data(String path, byte[] data, int version) {
		try {
			LOGGER.info("写入节点数据:".concat(path));
			Stat setData = zookeeper.setData(path, data, version);
			LOGGER.info("写入节点数据成功:".concat(path));
			return setData;
		} catch (Exception e) {
			LOGGER.error("写入节点数据失败: ", e);
		}
		return null;
	}

	public List<String> get_childrens(String path) {
		List<String> list = new ArrayList<String>();
		try {
			LOGGER.info("获取子节点目录:".concat(path));
			list = zookeeper.getChildren(path, false);
			LOGGER.info("获取子节点目录成功:".concat(path));
		} catch (Exception e) {
			LOGGER.error("获取子节点目录错误: ", e);
		}
		return list;
	}

	public List<String> get_childrens_data(String path) {
		List<String> list = new ArrayList<String>();
		try {
			LOGGER.info("获取子节点数据:".concat(path));
			List<String> childrens = zookeeper.getChildren(path, false);
			for (String service_path : childrens) {
				try {
					StringBuilder service_path_sbf = new StringBuilder(path);
					if (!path.equals("/")) {
						service_path_sbf.append("/");
					}
					service_path_sbf.append(service_path);
					byte[] data = zookeeper.getData(service_path_sbf.toString(), false, null);
					if (data != null) {
						list.add(new String(data));
					}
				} catch (Exception e) {
					LOGGER.error("获取字节点数据错误:", e);
				}
			}
			LOGGER.info("获取子节点数据成功:".concat(path));
		} catch (Exception e) {
			LOGGER.error("获取子节点数据错误: ", e);
		}
		return list;
	}

	// //////////////////////////////////////////////子节点 Watcher////////////////////////////////////////////////////////////////
	private Set<String> all_watcher_children_paths = new HashSet<String>();
	// 定时重试监听失败的服务
	private Set<String> rewatcher_children_paths = new HashSet<String>();
	// 子节点改变触发事件处理
	private Map<String, InfoGen_Zookeeper_Handle_Watcher_Children> watcher_children_handle_map = new HashMap<>();

	// 创建子节点监听,如果已存在该字节点的监听直接返回
	public void watcher_children_single(String path, InfoGen_Zookeeper_Handle_Watcher_Children watcher_children_handle) {
		if (watcher_children_handle_map.get(path) != null) {
			LOGGER.info("当前监听已经注册过:".concat(path));
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
			LOGGER.info("启动子节点监听:".concat(path));
			zookeeper.getChildren(path, (event) -> {
				LOGGER.info("子节点事件  path:" + event.getPath() + "  state:" + event.getState().name() + "  type:" + event.getType().name());
				if (event.getType() == EventType.NodeChildrenChanged) {
					LOGGER.info("子节点改变 重新启动子节点监听:".concat(path));
					watcher_children(path);

					InfoGen_Zookeeper_Handle_Watcher_Children watcher_children_handle = watcher_children_handle_map.get(path);
					if (watcher_children_handle != null) {
						watcher_children_handle.handle_event(path);
					}
				} else if (event.getType() == EventType.None) {
					// 连接状态改变
					// event.getType() == EventType.None
					if (event.getState() == KeeperState.Expired) {
						LOGGER.info("Session超时 子节点监听丢失 等待重启后重新监听:".concat(path));
					}
				} else if (event.getType() == EventType.NodeDeleted) {
					LOGGER.info("节点删除 重新启动子节点监听:".concat(path));
					watcher_children(path);
				} else {

				}
			});
			LOGGER.info("启动子节点监听成功:".concat(path));
		} catch (Exception e) {
			rewatcher_children_paths.add(path);
			LOGGER.error("启动子节点监听错误: ", e);
		}
	}

	// ///////////////////////////////////////连接 Watcher///////////////////////////////////////////////////
	/**
	 * 只对 Client 的连接状态变化做出反应
	 */
	private Watcher connect_watcher = new Watcher() {
		@Override
		public void process(WatchedEvent event) {
			LOGGER.info("连接事件  path:" + event.getPath() + "  state:" + event.getState().name() + "  type:" + event.getType().name());
			if (event.getType() == Watcher.Event.EventType.None) {
				switch (event.getState()) {
				case SyncConnected:
					break;
				case Expired:
					try {
						LOGGER.error("zookeeper 连接过期");
						rewatcher_children_paths_schedule.cancel(true);
						stop_zookeeper();
						start_zookeeper(host_port, expired_handle);

						LOGGER.info(" 重新加载子节点监听事件");
						rewatcher_children_paths.addAll(all_watcher_children_paths);
						rewatcher_childrens();

						LOGGER.info("其它定制处理");
						if (expired_handle != null) {
							expired_handle.handle_event();
						}
						rewatcher_children_paths_schedule = Scheduled.executors_single.scheduleWithFixedDelay(rewatcher_children_paths_runable, 16, 16, TimeUnit.SECONDS);
					} catch (Exception e) {
						LOGGER.error("zookeeper 重连错误", e);
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
	private byte[] rewatcher_children_lock = new byte[0];

	private void rewatcher_childrens() {
		synchronized (rewatcher_children_lock) {
			Set<String> paths = new HashSet<String>();
			paths.addAll(rewatcher_children_paths);
			for (String server_name : paths) {
				try {
					watcher_children(server_name);
					rewatcher_children_paths.remove(server_name);
				} catch (Exception e) {
					LOGGER.error("重新执行子节点监听", e);
				}
			}
		}
	}

	private final Runnable rewatcher_children_paths_runable = new Runnable() {
		@Override
		public void run() {
			rewatcher_childrens();
		}
	};
	// 定时修正监听失败
	protected ScheduledFuture<?> rewatcher_children_paths_schedule = Scheduled.executors_single.scheduleWithFixedDelay(rewatcher_children_paths_runable, 16, 16, TimeUnit.SECONDS);

}
