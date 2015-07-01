package com.infogen;

import com.larrylgq.aop.util.map.LRULinkedHashMap;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月29日 下午2:18:27
 * @since 1.0
 * @version 1.0
 */
public class LRU {
	public static void main(String[] args) {
		LRULinkedHashMap<Integer, String> map = new LRULinkedHashMap<>(10);
		for (int i = 0; i < 100; i++) {
			map.put(i, i + "");
		}

		for (String string : map.values()) {
			System.out.println(string);
		}
	}
}
