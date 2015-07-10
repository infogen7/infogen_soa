package com.infogen.limit.dao;

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
public class Redis_Group_DAO extends Group_DAO {
	private static final Logger LOGGER = Logger.getLogger(Redis_Group_DAO.class.getName());
	private JedisPool pool = null;

	private JedisPoolConfig getJedisPoolConfig() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(50);
		poolConfig.setMaxTotal(500);
		poolConfig.setMinIdle(10);
		poolConfig.setMaxWaitMillis(1000 * 100);
		return poolConfig;
	}

	public Redis_Group_DAO(String ip, Integer port) {
		LOGGER.info("#创建 redis 连接池");
		pool = new JedisPool(getJedisPoolConfig(), ip, port, 1000);
		LOGGER.info("#创建 redis 连接池成功");
	}

	public Redis_Group_DAO(String ip, Integer port, String password) {
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
	 * @see com.infogen.limit.dao.Group_DAO#increment_and_get(java.lang.String)
	 */
	private Integer expire = 60 * 60 * 12;

	@Override
	public Long increment_and_get(String group_by) {
		Jedis take = take();
		Long incr;
		try {
			incr = take.incr(group_by);
			take.expire(group_by, expire);
		} finally {
			returnResource(take);
		}
		return incr;
	}

}
