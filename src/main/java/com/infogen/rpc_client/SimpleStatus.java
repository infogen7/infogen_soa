package com.infogen.rpc_client;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月27日 下午6:06:54
 * @since 1.0
 * @version 1.0
 */
public class SimpleStatus {
	public RpcController controller;
	public Message responsePrototype;
	public RpcCallback<Message> callback;

	public SimpleStatus(RpcController controller, Message responsePrototype, RpcCallback<Message> callback) {
		super();
		this.controller = controller;
		this.responsePrototype = responsePrototype;
		this.callback = callback;
	}

}
