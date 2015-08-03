package com.infogen.rpc.handler;

import org.apache.thrift.TException;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;

import com.infogen.rpc.callback.RPC_Callback;

/**
 * 用于自定义thrift异步消息处理的接口
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年11月7日 上午10:54:29
 */
public interface Thrift_Async_Client_Handler<T> {
	public abstract RPC_Callback<T> handle_event(TProtocolFactory protocol, TAsyncClientManager cm, TNonblockingSocket async_transport,RPC_Callback<T> callback) throws TException;
}
