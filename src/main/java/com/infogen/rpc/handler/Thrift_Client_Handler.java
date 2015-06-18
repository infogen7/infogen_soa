package com.infogen.rpc.handler;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/**
 * thrift消息处理器
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年11月7日 上午10:54:29
 */
public interface Thrift_Client_Handler<T> {
	public abstract T handle_event(TProtocol protocol) throws TException;
}
