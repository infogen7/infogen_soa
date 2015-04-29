package com.infogen.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;

import com.infogen.InfoGen;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.logger.InfoGen_Logger_Kafka;
import com.infogen.security.InfoGen_Security;
import com.infogen.self_describing.component.OutParameter;
import com.infogen.util.NativePath;

/**
 * 用于启动mvc框架的监听器
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 */
public class InfoGen_Server_Listener implements WebApplicationInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.WebApplicationInitializer#onStartup(javax.servlet.ServletContext)
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		// 更新web.xml中的白名单
		ServletRegistration servletRegistration = servletContext.getServletRegistration("default");
		Collection<String> white_lists = new ArrayList<>();
		String contextPath = servletContext.getContextPath();
		for (String mapping : servletRegistration.getMappings()) {
			if (!mapping.startsWith(contextPath)) {
				mapping = contextPath.concat(mapping);
			}
			white_lists.add(mapping);
		}
		InfoGen_Security.getInstance().add_ignore(white_lists);
		// TODO 用于本机调试
		try {
			Properties service_properties = new Properties();
			try (InputStream resourceAsStream = Files.newInputStream(NativePath.get("infogen.properties"), StandardOpenOption.READ);//
					InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);) {
				service_properties.load(inputstreamreader);
			}
			InfoGen_Configuration config = new InfoGen_Configuration(service_properties);
			config.add_basic_outparameter(new OutParameter("note", String.class, false, "", "错误描述"));
			config.add_basic_outparameter(new OutParameter("code", Integer.class, true, "200", "错误码<br>200 成功<br>400 参数不正确<br>401 特定参数不符合条件(eg:没有这个用户)<br>404 没有这个方法 (RPC调用)<br>500 错误"));

			InfoGen.getInstance().start_and_watch(config).register();
			InfoGen_Logger_Kafka.getInstance().start(config);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
