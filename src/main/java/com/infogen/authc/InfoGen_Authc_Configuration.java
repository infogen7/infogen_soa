package com.infogen.authc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.infogen.configuration.InfoGen_Configuration;
import com.larrylgq.aop.tools.Tool_Core;
import com.larrylgq.aop.util.NativePath;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 上午11:49:48
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Authc_Configuration {
	private final static Logger LOGGER = Logger.getLogger(InfoGen_Authc_Configuration.class.getName());
	public final static ZoneId zoneid = ZoneId.of("GMT+08:00");
	public final static Charset charset = StandardCharsets.UTF_8;

	public InfoGen_Authc_Configuration(String authc_path) throws IOException {
		try (InputStream resourceAsStream = Files.newInputStream(NativePath.get(authc_path), StandardOpenOption.READ);//
				InputStreamReader inputstreamreader = new InputStreamReader(resourceAsStream, InfoGen_Configuration.charset);//
				BufferedReader reader = new BufferedReader(inputstreamreader)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = Tool_Core.trim(line);
				if (line.equals("[main]")) {

				}
			}
		}
	}

}
