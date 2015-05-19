package com.infogen.self_describing.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法,类,及参数的基本描述
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:17:28
 * @since 1.0
 * @version 1.0
 */
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Describe {
	public String author();

	public double version();

	public String value();

	public String tags() default "";
}
