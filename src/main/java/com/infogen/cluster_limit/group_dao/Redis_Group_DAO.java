package com.infogen.cluster_limit.group_dao;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月13日 下午5:06:34
 * @since 1.0
 * @version 1.0
 */
public class Redis_Group_DAO extends Group_DAO {
	private static final Logger LOGGER = Logger.getLogger(Redis_Group_DAO.class.getName());
	private JedisPool pool = null;

	public Redis_Group_DAO(JedisPool pool) {
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