package com.infogen.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@JsonIgnore
	private static final long serialVersionUID = 597069376404016921L;
	private RegisterServer server = new RegisterServer();
	private List<Function> functions = new ArrayList<>();

	public List<Function> getFunctions() {
		return functions;
	}

	public void setFunctions(List<Function> functions) {
		this.functions = functions;
	}

	public RegisterServer getServer() {
		return server;
	}

	public void setServer(RegisterServer server) {
		this.server = server;
	}

}
