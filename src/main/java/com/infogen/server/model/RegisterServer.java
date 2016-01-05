/**
 * 
 */
package com.infogen.server.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.infogen.configuration.InfoGen_Configuration;

/**
 * 服务基本属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:28:11
 * @since 1.0
 * @version 1.0
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RegisterServer implements Serializable {
	private static final long serialVersionUID = -8079754814055388959L;
	protected String name;
	protected String protocol = "";
	protected String describe = "";
	protected Integer min_nodes = 1;
	protected String http_proxy;// proxy domain
	
	// 不需要配置
	protected String infogen_version = "";
	// 不需要配置
	// 服务的元数据,支持的功能等个性化配置
	protected Map<String, Object> metadata = new HashMap<>();
	// 不需要配置
	protected Timestamp time = new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis());

	public Boolean available() {
		if (name == null || name.isEmpty()) {
			return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getHttp_proxy() {
		return http_proxy;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public void setHttp_proxy(String http_proxy) {
		this.http_proxy = http_proxy;
	}

	public Integer getMin_nodes() {
		return min_nodes;
	}

	public void setMin_nodes(Integer min_nodes) {
		this.min_nodes = min_nodes;
	}

	public String getInfogen_version() {
		return infogen_version;
	}

	public void setInfogen_version(String infogen_version) {
		this.infogen_version = infogen_version;
	}

}
