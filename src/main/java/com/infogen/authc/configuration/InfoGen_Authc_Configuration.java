package com.infogen.authc.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.util.Map;

import com.infogen.authc.configuration.handle.Properties_Handle;
import com.infogen.authc.configuration.handle.impl.Properties_Main_Handle;
import com.infogen.authc.configuration.handle.impl.Properties_Methods_Handle;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.self_describing.InfoGen_HTTP_Self_Describing;
import com.larrylgq.aop.AOP;
import com.larrylgq.aop.self_describing.component.Function;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.util.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 上午11:49:48
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Authc_Configuration {
	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

	public Properties_Handle properties_main = new Properties_Main_Handle();
	public Properties_Handle properties_methods = new Properties_Methods_Handle();
	public Map<String, Function> http_functions;

	public InfoGen_Authc_Configuration(String authc_path) throws IOException {
		this(authc_path, InfoGen_HTTP_Self_Describing.getInstance().self_describing(AOP.getInstance().getClasses()));
	}

	public InfoGen_Authc_Configuration(String authc_path, Map<String, Function> http_functions) throws IOException {
		this.http_functions = http_functions;
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get(authc_path), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);//
				BufferedReader reader = new BufferedReader(inputstreamreader)) {
			Properties_Handle properties_current = null;
			String line;
			while ((line = reader.readLine()) != null) {
				line = Tool_Core.trim(line);
				if (line.startsWith("#")) {
					continue;
				}
				if (line.equals("[main]")) {
					properties_current = properties_main;
				} else if (line.equals("[methods]")) {
					properties_current = properties_methods;
				} else {
					properties_current.handle(line);
				}
			}
		}
	}

}
