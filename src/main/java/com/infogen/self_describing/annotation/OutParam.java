package com.infogen.self_describing.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法的返回值描述
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:18:48
 * @since 1.0
 * @version 1.0
 */
@Repeatable(OutParameters.class)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OutParam {
	public String name() default "";

	public String describe() default "";

	public Class<?> type() default Object.class;

	public boolean required() default true;

	public String default_value() default "";
}
