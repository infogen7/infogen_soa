package com.infogen;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.testng.annotations.Test;

import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.InfoGen_Jetty;

public class InfoGen_JettyTest {

	@Test(groups = { "tools", "all" })
	public void start() throws IOException, URISyntaxException, InterruptedException {
		// Start jetty server before test method executed.
		Properties infogen_properties = new Properties();
		infogen_properties.setProperty("infogen.http.port", "9098");
		infogen_properties.setProperty("infogen.zookeeper", "127.0.0.1:2181");
		infogen_properties.setProperty("infogen.kafka", "127.0.0.1:10086");
		infogen_properties.setProperty("infogen.name", "uat2.com.infogen.UT");
		infogen_properties.setProperty("infogen.protocol", "http");
		infogen_properties.setProperty("infogen.server_room", "xuhui@youfu");
		infogen_properties.setProperty("infogen.ratio", "10");
		infogen_properties.setProperty("infogen.rpc.port", "10086");
		InfoGen_Configuration config = InfoGen_Configuration.getInstance().initialization(infogen_properties);
		InfoGen_Jetty.getInstance().start(config, "/", "src/main/webapp", "src/main/webapp/WEB-INF/web.xml");
		// Thread.currentThread().join();
	}
}
