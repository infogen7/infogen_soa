package com.infogen.cluster_limit.counter_dao;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.util.map.LRULinkedHashMap;

/**
 * 本地限流计数器实现
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Local_Counter_DAO extends Counter_DAO {
	private LRULinkedHashMap<String, AtomicLong> map = new LRULinkedHashMap<>(500000);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.limit.dao.Group_DAO#increment_and_get(java.lang.String)
	 */
	@Override
	public Long increment_and_get(String group_by, Integer timeslice) {
		long millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		timeslice = timeslice * 1000;
		group_by = new StringBuilder(group_by).append("_").append(millis - (millis % timeslice)).toString();
		AtomicLong atomic_long = map.get(group_by);
		if (atomic_long == null) {
			atomic_long = new AtomicLong(0);
			map.put(group_by, atomic_long);
		}
		return atomic_long.incrementAndGet();
	}

}
