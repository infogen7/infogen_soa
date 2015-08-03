package com.infogen.limit.configuration.handle;

/**
 * 并发数限流的ini配置文件解析器接口
 * @author larry/larrylv@outlook.com/创建时间 2015年6月19日 下午12:49:12
 * @since 1.0
 * @version 1.0
 */
public abstract class Limit_Properties_Handle {
	public abstract void handle(String line);
}
