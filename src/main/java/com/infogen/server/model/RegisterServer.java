/**
 * 
 */
package com.infogen.server.model;

import java.util.HashMap;
import java.util.Map;

import com.infogen.self_description.component.Function;

/**
 * 写入注册中心的服务扩展属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:29:20
 * @since 1.0
 * @version 1.0
 */
public class RegisterServer extends AbstractServer {
	private Map<String, Function> http_functions = new HashMap<>();

	public Map<String, Function> getHttp_functions() {
		return http_functions;
	}

	public void setHttp_functions(Map<String, Function> http_functions) {
		this.http_functions = http_functions;
	}

}
