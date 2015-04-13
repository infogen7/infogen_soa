/**
 * 
 */
package com.infogen.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;

import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infogen.http.InfoGen_HTTP;
import com.infogen.http.callback.Http_Callback;
import com.infogen.rpc.callback.RPC_Callback;
import com.infogen.thrift.Message;
import com.infogen.thrift.Message.AsyncClient;
import com.infogen.thrift.Message.AsyncClient.call_call;
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
	public static final Logger logger = Logger.getLogger(NativeNode.class.getName());
	@JsonIgnore
	private transient LongAdder sequence = new LongAdder();
	@JsonIgnore
	private transient Integer connect_timeout = 3000;

	public void clean() {
		client = null;
		if (transport.isOpen()) {
			transport.close();
		}
		asyncClient = null;
		if (async_transport.isOpen()) {
			async_transport.close();
		}
	}

	// ///////////////////////////////////////////rpc//////////////////////////////////////////////////
	@JsonIgnore
	private transient Message.Client client = null;
	@JsonIgnore
	private transient TTransport transport = null;
	@JsonIgnore
	private transient Message.AsyncClient asyncClient = null;
	@JsonIgnore
	private transient TNonblockingTransport async_transport = null;

	private Message.Client get_client() throws IOException, TTransportException {
		if (client == null) {
			TTransport transport = new TFramedTransport(new TSocket(ip, rpc_port, connect_timeout));
			TProtocol protocol = new TCompactProtocol(transport);
			client = new Message.Client(protocol);
			transport.open();
		}
		return client;
	}

	private Message.AsyncClient get_async_client() throws IOException {
		if (asyncClient == null) {
			TAsyncClientManager clientManager = new TAsyncClientManager();
			async_transport = new TNonblockingSocket(ip, rpc_port, connect_timeout);
			TProtocolFactory protocol = new TCompactProtocol.Factory();
			asyncClient = new Message.AsyncClient(protocol, clientManager, async_transport);
		}
		return asyncClient;
	}

	public Return call_once(String session, String method, List<BasicNameValuePair> name_value_pair) throws Exception {
		TTransport transport = new TSocket(ip, rpc_port, connect_timeout);
		TProtocol protocol = new TCompactProtocol(transport);
		Message.Client client = new Message.Client(protocol);
		try {
			transport.open();
			Request request = new Request();
			request.setSessionID(session);
			sequence.increment();
			request.setSequence(sequence.longValue());
			request.setMethod(method);
			Map<String, String> call_map = new HashMap<>();
			for (BasicNameValuePair basicNameValuePair : name_value_pair) {
				call_map.put(basicNameValuePair.getName(), basicNameValuePair.getValue());
			}
			request.setParameters(call_map);
			Response call = client.call(request);
			String data = call.getData();
			if (data == null) {
				return Return.SUCCESS(CODE._200.code, CODE._200.note);
			}
			return Return.create(data);
		} finally {
			transport.close();
		}
	}

	private static final StampedLock call_lock = new StampedLock();

	public Response call(String session, String method, Map<String, String> call_map) throws TException, IOException {
		Request request = new Request();
		request.setSessionID(session);
		sequence.increment();
		request.setSequence(sequence.longValue());
		request.setMethod(method);
		request.setParameters(call_map);
		Response call = null;

		long stamp = call_lock.writeLock();
		try {
			call = get_client().call(request);// TODO 使用前或定时测试连接可用性
		} catch (TException | IOException e) {
			client = null;
			transport.close();
			throw e;
		} finally {
			call_lock.unlockWrite(stamp);
		}
		return call;
	}

	private static final StampedLock async_call_lock = new StampedLock();

	public RPC_Callback async_call(String session, String method, Map<String, String> call_map) throws TException, IOException {
		RPC_Callback callback = new RPC_Callback();
		Request request = new Request();
		request.setSessionID(session);
		sequence.increment();
		request.setSequence(sequence.longValue());
		request.setMethod(method);
		request.setParameters(call_map);

		long stamp = async_call_lock.writeLock();
		try {
			get_async_client().call(request, new AsyncMethodCallback<AsyncClient.call_call>() {
				public void onComplete(call_call arg0) {
					try {
						callback.add(arg0.getResult());
					} catch (TException e) {
						onError(e);
					}
				}

				public void onError(Exception e) {
					Response call = new Response();
					call.success = false;
					call.code = CODE._500.code;
					call.note = e.getMessage();
					callback.add(call);
				}
			});
		} catch (Exception e) {
			asyncClient = null;
			async_transport.close();
			throw e;
		} finally {
			async_call_lock.unlockWrite(stamp);
		}
		//
		return callback;
	}

	// ///////////////////////////////////////////http////////////////////////////////////////////////
	public enum RequestType {
		POST, GET
	}

	public enum NetType {
		NET, LOCAL
	}

	public Http_Callback async_http(String method, List<BasicNameValuePair> name_value_pair, RequestType request_type, NetType net_type) throws IOException {
		StringBuffer async_http_sbf = new StringBuffer();
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

	public Return http(String method, List<BasicNameValuePair> name_value_pair, RequestType request_type, NetType net_type) throws IOException {
		StringBuffer blocking_http_sbf = new StringBuffer();
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
