package com.infogen.limit.dao;

import java.util.concurrent.atomic.AtomicLong;

import com.larrylgq.aop.util.map.LRULinkedHashMap;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Default_Group_DAO extends Group_DAO {
	private LRULinkedHashMap<String, AtomicLong> map = new LRULinkedHashMap<>(500000);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.limit.dao.Group_DAO#increment_and_get(java.lang.String)
	 */
	@Override
	public Long increment_and_get(String group_by) {
		// TODO Auto-generated method stub
		AtomicLong atomic_long = map.get(group_by);
		if (atomic_long == null) {
			atomic_long = new AtomicLong(0);
			map.put(group_by, atomic_long);
		}
		return atomic_long.incrementAndGet();
	}

}
