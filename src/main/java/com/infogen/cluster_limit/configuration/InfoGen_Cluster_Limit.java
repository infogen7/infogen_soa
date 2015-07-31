package com.infogen.cluster_limit.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.cluster_limit.InfoGen_HTTP_Cluster_Limit_Handle;
import com.infogen.cluster_limit.Limit_Model;
import com.infogen.cluster_limit.configuration_dao.Configuration_Sync_DAO;
import com.infogen.cluster_limit.group_dao.Group_DAO;
import com.infogen.util.Scheduled;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年7月13日 下午4:08:14
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Cluster_Limit {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Cluster_Limit.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Cluster_Limit instance = new InfoGen_Cluster_Limit();
	}

	public static InfoGen_Cluster_Limit getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Cluster_Limit() {
	}

	public static Configuration_Sync_DAO group_sync_dao;

	/**
	 * 限制集群接口一定窗口期内,按字段分组的调用次数
	 * 
	 * @param group_dao
	 * @param group_sync_dao
	 * @throws IOException
	 */
	public void limit(Group_DAO group_dao, Configuration_Sync_DAO group_sync_dao) throws IOException {
		InfoGen_HTTP_Cluster_Limit_Handle.group_dao = group_dao;

		Runnable group_sync_dao_runable = new Runnable() {
			@Override
			public void run() {
				Map<String, Limit_Model> load = group_sync_dao.update();
				if (load != null) {
					InfoGen_HTTP_Cluster_Limit_Handle.limit_models.putAll(load);
				}
			}
		};
		Scheduled.executors_single.scheduleWithFixedDelay(group_sync_dao_runable, 3, 3, TimeUnit.SECONDS);
		LOGGER.info("初始化权限配置");
	}
}
