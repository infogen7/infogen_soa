package com.infogen;

import java.util.Date;
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
		poolConfig.setMaxTotal(500);
		poolConfig.setMinIdle(10);
		poolConfig.setMaxWaitMillis(1000 * 100);

		// poolConfig.setMaxTotal(5000);
		// poolConfig.setMaxIdle(4000);
		// poolConfig.setTestOnBorrow(true);
		// poolConfig.setTestOnReturn(true);
		// poolConfig.setLifo(false);

		int timeout = 1000;
		pool = new JedisPool(poolConfig, "172.16.8.182", 6379, timeout, "cUVI32CaxF");
		System.out.println("start");
		for (int i = 0; i < 1; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					System.out.println("in");
					Jedis take = pool.getResource();
					try {
						System.out.println(new Date());
						take.blpop(3, "sadsad" + Math.random());// 阻塞
						System.out.println(new Date());
						// 设置10分钟自动删除
					} finally {
						pool.returnResourceObject(take);
					}
					System.out.println(Math.random());
					// http://localhost:9091/get?token=sadas
					// try {
					// InfoGen_HTTP.do_get("http://localhost:9091/get?token=sadas", null);
					// } catch (IOException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}
			});
			thread.start();
		}
	}
}
