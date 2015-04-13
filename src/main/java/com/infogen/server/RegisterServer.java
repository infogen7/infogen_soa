/**
 * 
 */
package com.infogen.server;

import java.util.HashMap;
import java.util.Map;

import com.infogen.self_describing.component.Function;

/**
 * 为注册服务扩展的服务属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月28日 上午10:03:46
 */
public class RegisterServer extends AbstractServer {
	private Map<String, Function> functions = new HashMap<>();

	public Map<String, Function> getFunctions() {
		return functions;
	}

	public void setFunctions(Map<String, Function> functions) {
		this.functions = functions;
	}

}
