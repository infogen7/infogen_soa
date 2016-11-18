/**
 * 
 */
package com.infogen.core.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Tool_Files {
	private static final Logger LOGGER = LogManager.getLogger(Tool_Files.class.getName());
	/**
	 * 创建文件并自动补全文件路径的缺失文件夹
	 * 
	 * @param paths
	 *            文件路径
	 */
	public static void prepare_files(Path... paths) {
		for (Path path : paths) {
			try {
				if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
					Files.createFile(path);
				}
			} catch (IOException e) {
				LOGGER.error("创建依赖文件夹失败", e);
			}
		}
	}

	/**
	 * @param path
	 *            文件路径
	 * @return 加载文件为字符串
	 * @throws IOException
	 *             读取文件异常
	 */
	public static String load_file(Path path) throws IOException {
		// 获取缓存的服务配置
		StringBuilder sbf = new StringBuilder();
		Files.lines(path, StandardCharsets.UTF_8).forEach(line -> {
			sbf.append(line);
		});
		return sbf.toString();
	}

}
