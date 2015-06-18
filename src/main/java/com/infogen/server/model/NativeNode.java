package com.infogen.server.model;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import scala.util.Random;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.InfoGen_HTTP;
import com.infogen.http.callback.Http_Callback;
import com.infogen.thrift.Message;
import com.infogen.thrift.Request;
import com.infogen.thrift.Response;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * 为本地调用处理扩展的节点属性
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月24日 下午4:27:17
 * @当前版本只支持 rest,rpc调用,只支持对等式
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class NativeNode extends AbstractNode {
	@JsonIgnore
	public Long disabled_time = Clock.system(InfoGen_Configuration.zoneid).millis();

	/**
	 * 不推荐使用
	 * 
	 * @param session
	 * @param method
	 * @param name_value_pair
	 * @return
	 * @throws TException
	 * @throws Exception
	 */

	public Return call_once(String session, String method, Map<String, String> name_value_pair) throws TException {
		TTransport transport = new TSocket(ip, rpc_port);
		TProtocol protocol = new TCompactProtocol(transport);
		Message.Client client = new Message.Client(protocol);
		try {
			transport.open();
			Request request = new Request();
			request.setSessionID(session);
			request.setSequence(new Random(Long.MAX_VALUE).nextLong());
			request.setMethod(method);
			Map<String, String> call_map = new HashMap<>();
			for (Entry<String, String> basicNameValuePair : name_value_pair.entrySet()) {
				call_map.put(basicNameValuePair.getKey(), basicNameValuePair.getValue());
			}
			request.setParameters(call_map);
			Response call = client.call(request);
			String data = call.getData();
			if (data == null) {
				return Return.SUCCESS(CODE.success.code, CODE.success.note);
			}
			return Return.create(data);
		} finally {
			transport.close();
		}
	}

	// ///////////////////////////////////////////http////////////////////////////////////////////////
	public enum RequestType {
		POST, GET
	}

	public enum NetType {
		NET, LOCAL
	}

	public Http_Callback async_http(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type) throws IOException {
		StringBuilder async_http_sbf = new StringBuilder();
		if (request_type == RequestType.GET) {
			if (net_type == NetType.LOCAL) {
				async_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				async_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			return InfoGen_HTTP.do_async_get(async_http_sbf.toString(), name_value_pair);
		} else {
			if (net_type == NetType.LOCAL) {
				async_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				async_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			return InfoGen_HTTP.do_async_post(async_http_sbf.toString(), name_value_pair);
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
