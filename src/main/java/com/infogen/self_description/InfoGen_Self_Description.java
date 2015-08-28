package com.infogen.self_description;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import com.infogen.self_description.component.Function;
import com.infogen.self_description.parser.HTTP_Parser;

/**
 * HTTP协议中启动时扫描自描述配置
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月21日 下午5:20:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Self_Description {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_Self_Description.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Self_Description instance = new InfoGen_Self_Description();
	}

	public static InfoGen_Self_Description getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Self_Description() {
	}

	private HTTP_Parser http_parser = new HTTP_Parser();

	public Map<String, Function> self_description(Set<Class<?>> class_set) {
		Map<String, Function> functions = new HashMap<>();
		class_set.forEach((clazz) -> {
			try {
				RestController rest_controller = clazz.getAnnotation(RestController.class);
				if (rest_controller != null) {
					http_parser.self_description(clazz);
				}

			} catch (Exception e) {
				LOGGER.error("解析class失败:", e);
			}
		});
		return functions;
	}

}