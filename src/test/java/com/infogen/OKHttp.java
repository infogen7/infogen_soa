/**
 * @author larry/larrylv@outlook.com
 * @date 创建时间 2015年6月4日 上午11:10:29
 * @version 1.0
 */
package com.infogen;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Properties;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.InfoGen_HTTP;
import com.infogen.http.InfoGen_Jetty;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月4日 上午11:10:29
 * @since 1.0
 * @version 1.0
 */
public class OKHttp {
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
		Properties infogen_properties = new Properties();
		infogen_properties.setProperty("infogen.http.port", "9091");
		infogen_properties.setProperty("infogen.zookeeper", "127.0.0.1:2181");
		infogen_properties.setProperty("infogen.name", "uat2.com.infogen.UT");
		InfoGen_Configuration config = InfoGen_Configuration.getInstance().initialization(infogen_properties);
		InfoGen_Jetty.getInstance().start(config, "/", "src/main/webapp", "src/main/webapp/WEB-INF/web.xml");

		Thread.currentThread().sleep(3000);

		long start_millis = Clock.system(InfoGen_Configuration.zoneid).millis();
		for (int i = 0; i < 1000; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						InfoGen_HTTP.do_get("http://localhost:9091/get?token=123", null);
					} catch (IOException e) {
						e.printStackTrace();
					}
					long last_invoke_millis = Clock.system(InfoGen_Configuration.zoneid).millis();
					System.out.println(last_invoke_millis - start_millis);
				}
			});
			thread.start();
		}
		Thread.currentThread().join();
	}
}
