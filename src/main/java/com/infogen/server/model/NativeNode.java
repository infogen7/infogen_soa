package com.infogen.server.model;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

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
import com.infogen.http.Tool_HTTP;
import com.infogen.http.callback.Http_Callback;
import com.infogen.rpc.callback.RPC_Callback;
import com.infogen.rpc.handler.Thrift_Async_Client_Handler;
import com.infogen.rpc.handler.Thrift_Client_Handler;

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

	@JsonIgnore
	private static final Integer connect_timeout = 3_000;// 连接时间
	@JsonIgnore
	private static final StampedLock call_lock = new StampedLock();
	@JsonIgnore
	private static final StampedLock call_async_lock = new StampedLock();

	@JsonIgnore
	private transient TTransport transport = null;
	@JsonIgnore
	private transient TNonblockingSocket async_transport = null;

	public TTransport get_transport() throws IOException, TTransportException {
		if (transport == null) {
			transport = new TFramedTransport(new TSocket(ip, rpc_port, connect_timeout));
		}
		if (!transport.isOpen()) {
			transport.open();
		}
		return transport;
	}

	public TNonblockingSocket get_async_transport() throws IOException, TTransportException {
		if (async_transport == null) {
			async_transport = new TNonblockingSocket(ip, rpc_port, connect_timeout);
		}
		return async_transport;
	}

	public <T> T call(Thrift_Client_Handler<T> handle) throws TException, IOException {
		T handle_event;
		long stamp = call_lock.writeLock();
		try {
			TProtocol protocol = new TCompactProtocol(get_transport());
			handle_event = handle.handle_event(protocol);
		} catch (IOException e) {
			transport.close();
			transport = null;
			throw e;
		} finally {
			call_lock.unlockWrite(stamp);
		}
		return handle_event;
	}

	public <T> RPC_Callback<T> call_async(Thrift_Async_Client_Handler<T> handle) throws TException, IOException {
		RPC_Callback<T> callback = new RPC_Callback<>();
		TAsyncClientManager clientManager = new TAsyncClientManager();
		TProtocolFactory protocol = new TCompactProtocol.Factory();
		RPC_Callback<T> handle_event;

		long stamp = call_async_lock.writeLock();
		try {
			TNonblockingSocket get_async_transport = get_async_transport();
			handle_event = handle.handle_event(protocol, clientManager, get_async_transport, callback);
		} catch (IOException e) {
			async_transport.close();
			async_transport = null;
			throw e;
		} finally {
			call_async_lock.unlockWrite(stamp);
		}
		return handle_event;
	}

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
			return Tool_HTTP.do_async_get(async_http_sbf.toString(), name_value_pair);
		} else {
			if (net_type == NetType.LOCAL) {
				async_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				async_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			return Tool_HTTP.do_async_post(async_http_sbf.toString(), name_value_pair);
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
			return Tool_HTTP.do_get(blocking_http_sbf.toString(), name_value_pair);
		} else {
			if (net_type == NetType.LOCAL) {
				blocking_http_sbf.append(http_protocol).append("://").append(ip).append(":").append(http_port).append("/").append(method);
			} else {
				blocking_http_sbf.append(http_protocol).append("://").append(net_ip).append(":").append(http_port).append("/").append(method);
			}
			return Tool_HTTP.do_post(blocking_http_sbf.toString(), name_value_pair);
		}
	}

}
