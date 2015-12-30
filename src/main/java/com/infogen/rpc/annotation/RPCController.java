package com.infogen.rpc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月31日 下午5:05:17
 * @since 1.0
 * @version 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCController {
	// 对应 proto 文件中的 service 名称
	String value();
}
