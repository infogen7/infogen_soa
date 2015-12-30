package com.infogen.self_description;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.core.structure.DefaultEntry;
import com.infogen.self_description.component.Function;

/**
 * HTTP协议中启动时扫描自描述配置
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月21日 下午5:20:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Self_Description {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Self_Description.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Self_Description instance = new InfoGen_Self_Description();
	}

	public static InfoGen_Self_Description getInstance() {
		return InnerInstance.instance;
	}

	private InfoGen_Self_Description() {
	}

	public List<Function> self_description(Set<Class<?>> class_set, List<DefaultEntry<Class<? extends Annotation>, Self_Description>> list) {
		List<Function> functions = new ArrayList<>();
		class_set.forEach((clazz) -> {
			try {
				for (DefaultEntry<Class<? extends Annotation>, Self_Description> entry : list) {
					Annotation annotation = clazz.getAnnotation(entry.getKey());
					if (annotation != null) {
						functions.addAll(entry.getValue().self_description(clazz));
					}
				}
			} catch (Exception e) {
				LOGGER.error("解析class失败:", e);
			}
		});
		return functions;
	}

}
