/**
 * 
 */
package com.infogen.server.model;

import java.util.HashMap;
import java.util.Map;

import com.larrylgq.aop.self_describing.component.Function;

/**
 * 为注册服务扩展的服务属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月28日 上午10:03:46
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
