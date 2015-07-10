package com.infogen.limit.configuration;

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

import com.infogen.authc.configuration.handle.Authc_Properties_Handle;
import com.infogen.limit.configuration.handle.impl.Limit_Properties_Handle_Limit;
import com.infogen.limit.configuration.handle.impl.Limit_Properties_Handle_Main;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.util.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 上午11:49:48
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Limit {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Limit.class.getName());
	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

	private static class InnerInstance {
		public static final InfoGen_Limit instance = new InfoGen_Limit();
	}

	public static InfoGen_Limit getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Limit() {
	}

	private final Limit_Properties_Handle_Main properties_main = new Limit_Properties_Handle_Main();
	private final Limit_Properties_Handle_Limit properties_limit = new Limit_Properties_Handle_Limit();

	public void limit(String limit_path) throws IOException {
		load_configuration(limit_path);
		LOGGER.info("初始化权限配置");
	}

	private void load_configuration(String limit_path) throws IOException {
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get(limit_path), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, charset);//
				BufferedReader reader = new BufferedReader(inputstreamreader)) {
			Authc_Properties_Handle properties_current = null;
			String line;
			while ((line = reader.readLine()) != null) {
				line = Tool_Core.trim(line);
				if (line.startsWith("#")) {
					continue;
				} else if (line.equals("[main]")) {
					properties_current = properties_main;
				} else if (line.equals("[limit]")) {
					properties_current = properties_limit;
				} else if (line != null && !line.isEmpty()) {
					properties_current.handle(line);
				} else {

				}
			}
		}
	}

}
