package com.infogen;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis {
	public static ScheduledExecutorService executors = Executors.newScheduledThreadPool(100);
	private static JedisPool pool;

	public static void main(String[] args) {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(50);
		poolConfig.setMaxTotal(200);
		poolConfig.setMinIdle(10);
		poolConfig.setMaxWaitMillis(1000 * 100);

		int timeout = 10000;
		pool = new JedisPool(poolConfig, "192.168.0.145", 6379, timeout, "redis2014!!");
		System.out.println("start");
		for (int i = 0; i < 1000; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					System.out.println("in");
					Jedis take = pool.getResource();
					try {
						take.blpop(3, "sadsad" + Math.random());// 阻塞
						// 设置10分钟自动删除
					} finally {
						pool.returnResourceObject(take);
					}
					System.out.println(Math.random());
				}
			});
			thread.start();
		}
	}
}
