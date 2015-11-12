package com.infogen.server.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.infogen.self_description.component.Function;

/**
 * 写入注册中心的服务扩展属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:29:20
 * @since 1.0
 * @version 1.0
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class ServiceFunctions implements Serializable {
	private static final long serialVersionUID = 597069376404016921L;
	private RegisterServer server = new RegisterServer();
	private Map<String, Function> http_functions = new HashMap<>();

	public Map<String, Function> getHttp_functions() {
		return http_functions;
	}

	public void setHttp_functions(Map<String, Function> http_functions) {
		this.http_functions = http_functions;
	}

	public RegisterServer getServer() {
		return server;
	}

	public void setServer(RegisterServer server) {
		this.server = server;
	}

}
