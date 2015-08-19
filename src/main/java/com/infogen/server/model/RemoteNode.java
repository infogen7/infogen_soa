package com.infogen.server.model;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.InfoGen_HTTP;
import com.squareup.okhttp.Callback;

import net.jcip.annotations.ThreadSafe;

/**
 * 为本地调用处理扩展的节点属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月17日 下午5:30:01
 * @since 1.0
 * @version 1.0
 */
@ThreadSafe
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RemoteNode extends AbstractNode {
	@JsonIgnore
	private final Logger LOGGER = Logger.getLogger(RemoteNode.class.getName());
	@JsonIgnore
	public transient Long disabled_time = Clock.system(InfoGen_Configuration.zoneid).millis();

	@JsonIgnore
	private final transient Integer connect_timeout = 3_000;// 连接超时时间

	/**
	 * 清理占用的资源
	 */
	public void clean() {
	}

	// ///////////////////////////////////////////http////////////////////////////////////////////////
	public enum RequestType {
		POST, GET
	}

	public enum NetType {
		NET, LOCAL
	}

	public void http_async(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type, Callback callback) throws IOException {
		StringBuilder async_http_sbf = new StringBuilder();
		if (request_type == RequestType.GET) {
			if (net_type == NetType.LOCAL) {
				async_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				async_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			InfoGen_HTTP.do_async_get(async_http_sbf.toString(), name_value_pair, callback);
		} else {
			if (net_type == NetType.LOCAL) {
				async_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				async_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			InfoGen_HTTP.do_async_post(async_http_sbf.toString(), name_value_pair, callback);
		}
	}

	public String http(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type) throws IOException {
		StringBuilder blocking_http_sbf = new StringBuilder();
		if (request_type == RequestType.GET) {
			if (net_type == NetType.LOCAL) {
				blocking_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				blocking_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			return InfoGen_HTTP.do_get(blocking_http_sbf.toString(), name_value_pair);
		} else {
			if (net_type == NetType.LOCAL) {
				blocking_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				blocking_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			return InfoGen_HTTP.do_post(blocking_http_sbf.toString(), name_value_pair);
		}
	}

}
