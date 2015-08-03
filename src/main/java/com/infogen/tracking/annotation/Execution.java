package com.infogen.tracking.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.infogen.tracking.enum0.Function_Type;

/**
 * 统计方法执行时间和调用链记录的注解
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:06:40
 * @since 1.0
 * @version 1.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Execution {
	Function_Type type() default Function_Type.INTERFACE;

	String user_definition() default "";
}
