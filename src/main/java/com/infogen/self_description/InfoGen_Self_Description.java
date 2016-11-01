/**
 * 
 */
package com.infogen.self_description;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.aop.AOP;
import com.infogen.core.structure.DefaultEntry;
import com.infogen.self_description.component.Function;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Self_Description {
	private final static Logger LOGGER = LogManager.getLogger(InfoGen_Self_Description.class.getName());

	private static class InnerInstance {
		public static final InfoGen_Self_Description instance = new InfoGen_Self_Description();
	}

	public static InfoGen_Self_Description getInstance() {
		return InnerInstance.instance;
	}

	private List<DefaultEntry<Class<? extends Annotation>, InfoGen_Parser>> defaultentrys = new ArrayList<>();

	public void add_parser(Class<? extends Annotation> clazz, InfoGen_Parser parser) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(parser);
		defaultentrys.add(new DefaultEntry<Class<? extends Annotation>, InfoGen_Parser>(clazz, parser));
	}
	
	public List<Function> parser(){
		List<Function> functions = new ArrayList<>();
		AOP.getInstance().getClasses().forEach((clazz) -> {
			try {
				for (DefaultEntry<Class<? extends Annotation>, InfoGen_Parser> entry : defaultentrys) {
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
