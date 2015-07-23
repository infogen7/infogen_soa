package com.infogen.server.model;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.configuration.InfoGen_Configuration;
import com.infogen.http.InfoGen_HTTP;
import com.infogen.http.callback.Http_Callback;
import com.infogen.rpc.callback.RPC_Callback;
import com.infogen.rpc.handler.Thrift_Async_Client_Handler;
import com.infogen.rpc.handler.Thrift_Client_Handler;

/**
 * 为本地调用处理扩展的节点属性
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月17日 下午5:30:01
 * @since 1.0
 * @version 1.0
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class RemoteNode extends AbstractNode {
	@JsonIgnore
	public Long disabled_time = Clock.system(InfoGen_Configuration.zoneid).millis();

	@JsonIgnore
	private static final Integer connect_timeout = 3_000;// 连接超时时间
	@JsonIgnore
	private final String call_lock = "";
	@JsonIgnore
	private final String call_async_lock = "";

	@JsonIgnore
	private transient TTransport transport = null;
	@JsonIgnore
	private transient TNonblockingSocket async_transport = null;

	/**
	 * 清理占用的资源
	 */
	public void clean() {
		clean_transport();
		clean_async_transport();
	}

	private void clean_transport() {
		synchronized (call_lock) {
			if (transport != null && transport.isOpen()) {
				transport.close();
			}
			transport = null;
		}
	}

	private void clean_async_transport() {
		synchronized (call_async_lock) {
			if (async_transport != null && async_transport.isOpen()) {
				async_transport.close();
			}
			async_transport = null;
		}
	}

	public TTransport get_transport() throws IOException, TTransportException {
		synchronized (call_lock) {
			if (transport == null) {
				transport = new TFramedTransport(new TSocket(ip, rpc_port, connect_timeout));
			}
			if (!transport.isOpen()) {
				transport.open();
			}
		}
		return transport;
	}

	public TNonblockingSocket get_async_transport() throws IOException, TTransportException {
		synchronized (call_async_lock) {
			if (async_transport == null) {
				async_transport = new TNonblockingSocket(ip, rpc_port, connect_timeout);
			}
		}
		return async_transport;
	}

	public <T> T call(Thrift_Client_Handler<T> handle) throws TException, IOException {
		T handle_event;
		try {
			TProtocol protocol = new TCompactProtocol(get_transport());
			synchronized (call_lock) {
				handle_event = handle.handle_event(protocol);
			}
		} catch (IOException e) {
			clean_transport();
			throw e;
		}
		return handle_event;
	}

	public <T> RPC_Callback<T> call_async(Thrift_Async_Client_Handler<T> handle) throws TException, IOException {
		RPC_Callback<T> callback = new RPC_Callback<>();
		TAsyncClientManager clientManager = new TAsyncClientManager();
		TProtocolFactory protocol = new TCompactProtocol.Factory();
		RPC_Callback<T> handle_event;

		try {
			TNonblockingSocket get_async_transport = get_async_transport();
			synchronized (call_async_lock) {
				handle_event = handle.handle_event(protocol, clientManager, get_async_transport, callback);
			}
		} catch (IOException e) {
			clean_async_transport();
			throw e;
		}
		return handle_event;
	}

	@Deprecated
	public <T> T call_once(Thrift_Client_Handler<T> handle) throws TException {
		TTransport transport = new TSocket(ip, rpc_port);
		TProtocol protocol = new TCompactProtocol(transport);
		transport.open();
		try {
			return handle.handle_event(protocol);
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

	public Http_Callback http_async(String method, Map<String, String> name_value_pair, RequestType request_type, NetType net_type) throws IOException {
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