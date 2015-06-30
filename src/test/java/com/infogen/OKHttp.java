/**
 * @author larry/larrylv@outlook.com
 * @date 创建时间 2015年6月4日 上午11:10:29
 * @version 1.0
 */
package com.infogen;

import java.io.IOException;
import java.time.Clock;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.Tool_HTTP;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月4日 上午11:10:29
 * @since 1.0
 * @version 1.0
 */
public class OKHttp {
	public static void main(String[] args) {
		long start_millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		for (int i = 0; i < 1000; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						Tool_HTTP.do_get("http://localhost:9091/get?token=123", null);
					} catch (IOException e) {
						e.printStackTrace();
					}
					long last_invoke_millis = Clock.system(InfoGen_Configuration.zoneid).millis();
					System.out.println(last_invoke_millis - start_millis);
				}
			});
			thread.start();
		}

	}
}
