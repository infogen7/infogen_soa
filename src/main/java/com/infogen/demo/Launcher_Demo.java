package com.infogen.demo;

/**
 * @author larry
 * @email   larrylv@outlook.com
 * @version 创建时间 2014年10月22日 下午6:55:23
 */
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.infogen.InfoGen;
import com.infogen.InfoGen_Jetty;
import com.infogen.InfoGen_Thrift;
import com.infogen.aop.event_handle.InfoGen_AOP_Handle;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.logger.InfoGen_Logger_Kafka;
import com.infogen.self_describing.component.OutParameter;
import com.infogen.util.NativePath;

public class Launcher_Demo {
	public static final Logger logger = Logger.getLogger(Launcher_Demo.class.getName());

	public static void main(String[] args) throws Exception {

		Properties service_properties = new Properties();
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get("infogen.properties"), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);) {
			service_properties.load(inputstreamreader);
		}
		InfoGen_Configuration config = new InfoGen_Configuration(service_properties);
		config.add_basic_outparameter(new OutParameter("note", String.class, false, "", "错误描述"));
		config.add_basic_outparameter(new OutParameter("code", Integer.class, true, "200", "错误码<br>200 成功<br>400 参数不正确<br>401 特定参数不符合条件(eg:没有这个用户)<br>404 没有这个方法 (RPC调用)<br>500 错误"));

		// 读取白名单
		InfoGen.getInstance().start_and_watch(config).register().start_white_list();

		InfoGen_Logger_Kafka.getInstance().start(config);
		InfoGen_Jetty.getInstance().start(config, "/", NativePath.get("webapp").toString(), NativePath.get("webapp/WEB-INF/web.xml").toString());
		InfoGen_Thrift.getInstance().start_asyn(config);

		//Logger_Handle.producer = null;
		//
		Thread.currentThread().join();
	}
}
