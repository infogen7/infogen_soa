package com.infogen.cluster_limit.configuration_dao;

import java.util.Map;

import com.infogen.cluster_limit.Limit_Model;

/**
 * 用于增量更新限流配置的接口,注:数据库中的配置数据只更新状态不要删除
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月13日 下午3:53:19
 * @since 1.0
 * @version 1.0
 */
public abstract class Configuration_Sync_DAO {
	public abstract Map<String, Limit_Model> update();
}
