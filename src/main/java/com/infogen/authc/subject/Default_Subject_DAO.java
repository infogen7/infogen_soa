package com.infogen.authc.subject;

import org.apache.log4j.Logger;

import com.infogen.cache.LRULinkedHashMap;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Default_Subject_DAO extends Subject_DAO {
	public static final Logger logger = Logger.getLogger(Default_Subject_DAO.class.getName());
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
