package com.infogen.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.infogen.http.InfoGen_HTTP;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月13日 下午2:08:49
 * @since 1.0
 * @version 1.0
 */
public class httpasyncclient_Client {
	public static void main(String[] args) throws InterruptedException {
		List<BasicNameValuePair> name_value_pair = new ArrayList<>();
		name_value_pair.add(new BasicNameValuePair("key", "value"));
		for (int i = 0; i < 10000; i++) {
			try {
				InfoGen_HTTP.do_async_get("www.baidu.com", name_value_pair);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		Thread.currentThread().join();
	}
}
