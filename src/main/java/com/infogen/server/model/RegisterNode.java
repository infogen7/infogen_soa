/**
 * 
 */
package com.infogen.server.model;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.core.structure.map.consistent_hash.ShardInfo;

/**
 * 节点基本属性 继承了用于一致性hash算法的接口
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:27:18
 * @since 1.0
 * @version 1.0
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RegisterNode extends ShardInfo {
	@JsonIgnore
	private static final long serialVersionUID = -5382412471335577005L;
	protected String ip;
	protected Integer http_port;
	protected Integer rpc_port;

	// 不需要配置
	protected String server_name;
	// 不需要配置
	protected String host;// 机器名
	// 不需要配置
	// 节点的元数据,支持的功能等个性化配置
	protected Map<String, Object> metadata = new HashMap<>();
	// 不需要配置
	protected Timestamp time = new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis());

	public Boolean available() {
		if (ratio == null || name == null || name.isEmpty() || ip == null || ip.isEmpty()) {
			return false;
		} else if (http_port == null && rpc_port == null) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getServer_name() {
		return server_name;
	}

	public void setServer_name(String server_name) {
		this.server_name = server_name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getHttp_port() {
		return http_port;
	}

	public void setHttp_port(Integer http_port) {
		this.http_port = http_port;
	}

	public Integer getRpc_port() {
		return rpc_port;
	}

	public void setRpc_port(Integer rpc_port) {
		this.rpc_port = rpc_port;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
