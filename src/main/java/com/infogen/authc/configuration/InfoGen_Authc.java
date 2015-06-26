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

import org.apache.log4j.Logger;

import com.infogen.authc.configuration.handle.Properties_Handle;
import com.infogen.authc.configuration.handle.impl.Properties_Main_Handle;
import com.infogen.authc.configuration.handle.impl.Properties_Methods_Handle;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.util.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 上午11:49:48
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Authc {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Authc.class.getName());
	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

	private static class InnerInstance {
		public static final InfoGen_Authc instance = new InfoGen_Authc();
	}

	public static InfoGen_Authc getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Authc() {
	}

	private final Properties_Handle properties_main = new Properties_Main_Handle();
	private final Properties_Methods_Handle properties_methods = new Properties_Methods_Handle();

	public void authc(String authc_path) throws IOException {
		load_configuration(authc_path);
		LOGGER.info("初始化权限配置");
	}

	private void load_configuration(String authc_path) throws IOException {
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get(authc_path), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, charset);//
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
