package com.infogen.authc.subject;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Redis_Subject_DAO extends Subject_DAO {
	private static final Logger LOGGER = Logger.getLogger(Redis_Subject_DAO.class.getName());
	private JedisPool pool = null;

	private JedisPoolConfig getJedisPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(50);
		poolConfig.setMaxTotal(500);
		poolConfig.setMinIdle(10);
		poolConfig.setMaxWaitMillis(1000 * 100);
		return poolConfig;
	}

	public Redis_Subject_DAO(String ip, Integer port) {
		LOGGER.info("#创建 redis 连接池");
		pool = new JedisPool(getJedisPoolConfig(), ip, port, 1000);
		LOGGER.info("#创建 redis 连接池成功");
	}

	public Redis_Subject_DAO(String ip, Integer port, String password) {
		LOGGER.info("#创建 redis 连接池");
		pool = new JedisPool(getJedisPoolConfig(), ip, port, 1000, password);
		LOGGER.info("#创建 redis 连接池成功");
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
			take.expire(key, 60 * 60 * 12);
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
	public void delete(String user) {
		Jedis take = take();
		try {
			take.expire(user, 1);
		} finally {
			returnResource(take);
		}
	}

}
