package com.infogen.self_describing.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支持多个OutParam
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:18:59
 * @since 1.0
 * @version 1.0
 */
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OutParameters {
	public OutParam[] value() default {};
}
