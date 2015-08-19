package com.infogen.authc.subject.dao;

import com.infogen.authc.subject.Subject;
import com.infogen.core.util.map.LRULinkedHashMap;

/**
 * 默认的基于本地内存的session管理器实现
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Local_Subject_DAO extends Subject_DAO {
	private LRULinkedHashMap<String, Subject> map = new LRULinkedHashMap<>(500000);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.cache.Subject_DAO#save(com.infogen.authc.subject.Subject)
	 */
	@Override
	public void save(Subject subject) {
		map.put(subject.getSubject(), subject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.cache.Subject_DAO#get(java.lang.String)
	 */
	@Override
	public Subject get(String subject_name) {
		return map.get(subject_name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.cache.Subject_DAO#delete(java.lang.String)
	 */
	@Override
	public void delete(String subject_name) {
		map.remove(subject_name);
	}

}
