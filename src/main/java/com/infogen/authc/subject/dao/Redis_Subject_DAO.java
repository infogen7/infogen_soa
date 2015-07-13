package com.infogen.authc.subject.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.infogen.authc.subject.Subject;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Redis_Subject_DAO extends Subject_DAO {
	private static final Logger LOGGER = Logger.getLogger(Redis_Subject_DAO.class.getName());
	private JedisPool pool = null;

	public Redis_Subject_DAO(JedisPool pool) {
		super();
		this.pool = pool;
	}

	public Jedis take() {
		if (pool == null) {
			LOGGER.error("Redis 没有初始化");
			return null;
		}
		return pool.getResource();
	}

	public void returnResource(Jedis redis) {
		pool.returnResourceObject(redis);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.cache.Subject_DAO#save(com.infogen.authc.subject.Subject)
	 */
	private Integer expire = 60 * 60 * 12;

	@Override
	public void save(Subject subject) {
		Jedis take = take();
		Map<String, String> map = new HashMap<>();
		map.put("remember", subject.getRemember().toString());
		map.put("roles", subject.getRoles());
		map.put("issued_at", subject.getIssued_at().toString());
		map.put("last_access_time", subject.getLast_access_time().toString());
		try {
			String key = subject.getSubject();
			take.hmset(key, map);
			take.expire(key, expire);
		} finally {
			returnResource(take);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.cache.Subject_DAO#get(java.lang.String)
	 */
	@Override
	public Subject get(String subject_name) {
		Jedis take = take();
		try {
			Map<String, String> hgetAll = take.hgetAll(subject_name);
			if (hgetAll != null) {
				Subject subject = new Subject();
				subject.setAudience(hgetAll.get("audience"));
				subject.setIssued_at(Long.valueOf(hgetAll.get("issued_at")));
				subject.setLast_access_time(Long.valueOf(hgetAll.get("last_access_time")));
				subject.setRemember(Boolean.valueOf(hgetAll.get("remember")));
				subject.setRoles(hgetAll.get("roles"));
				subject.setSubject(subject_name);
				return subject;
			}
		} finally {
			returnResource(take);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.cache.Subject_DAO#delete(java.lang.String)
	 */
	@Override
	public void delete(String subject_name) {
		Jedis take = take();
		try {
			take.hdel(subject_name);
		} finally {
			returnResource(take);
		}
	}

}
