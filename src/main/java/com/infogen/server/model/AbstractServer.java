/**
 * 
 */
package com.infogen.server.model;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import com.infogen.configuration.InfoGen_Configuration;

/**
 * 服务基本属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月29日 下午4:09:16
 */
public abstract class AbstractServer {
	protected String path;
	protected String name;
	protected String protocol = "rest";
	protected String describe = "";
	protected Integer min_nodes = 1;

	protected String infogen_version = "V1.0.03R150615";
	// proxy
	protected String http_domain;
	protected String http_proxy;
	// 服务的元数据,支持的功能等个性化配置
	protected Map<String, Object> metadata = new HashMap<>();

	protected Timestamp time = new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis());

	public Boolean available() {
		if (name == null) {
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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

	public String getHttp_domain() {
		return http_domain;
	}

	public void setHttp_domain(String http_domain) {
		this.http_domain = http_domain;
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
