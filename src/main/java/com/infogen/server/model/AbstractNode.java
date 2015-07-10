/**
 * 
 */
package com.infogen.server.model;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import com.infogen.configuration.InfoGen_Configuration;
import com.larrylgq.aop.util.map.consistent_hash.ShardInfo;

/**
 * 节点基本属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月29日 下午4:11:30
 */
public abstract class AbstractNode extends ShardInfo {
	
	protected String path;

	protected String server_room = "";// sh@youfu
	protected String host;// 机器名

	protected String ip;
	protected String net_ip;
	protected String http_protocol;
	protected Integer http_port;
	protected Integer rpc_port;

	protected String context = "";// tomcat的context path,使用tomcat的应该配置，jetty如果有特殊的路径，也可以配置

	// 节点的元数据,支持的功能等个性化配置
	protected Map<String, Object> metadata = new HashMap<>();

	protected Timestamp time = new Timestamp(Clock.system(InfoGen_Configuration.zoneid).millis());

	public Boolean available() {
		if (ratio == null || name == null || name.isEmpty() || ip == null || ip.isEmpty() || path == null || path.isEmpty()) {
			return false;
		} else if (http_port == null && rpc_port == null) {
			return false;
		}
		return true;
	}

	public String getServer_room() {
		return server_room;
	}

	public void setServer_room(String server_room) {
		this.server_room = server_room;
	}



	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHttp_protocol() {
		return http_protocol;
	}

	public void setHttp_protocol(String http_protocol) {
		this.http_protocol = http_protocol;
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

	public String getNet_ip() {
		return net_ip;
	}

	public void setNet_ip(String net_ip) {
		this.net_ip = net_ip;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String Context) {
		this.context = Context;
	}

}
