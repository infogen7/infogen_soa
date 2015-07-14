package com.infogen.cluster_limit.configuration_dao;

import java.util.Map;

import com.infogen.cluster_limit.Limit_Model;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年7月13日 下午3:53:19
 * @since 1.0
 * @version 1.0
 */
public abstract class Configuration_Sync_DAO {
	public abstract Boolean check();

	// 方法名->group key->group value->limit
	public abstract Map<String, Limit_Model> load();
}
