package com.infogen.rpc.client;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月26日 下午6:58:32
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_RPCController implements RpcController {
	private final AtomicReference<String> reason = new AtomicReference<>();
	private final AtomicBoolean failed = new AtomicBoolean();
	private final AtomicBoolean canceled = new AtomicBoolean();
	private final AtomicReference<RpcCallback<Object>> callback = new AtomicReference<>();

	///////////////////////////////// failed/////////////////////////
	@Override
	public void setFailed(final String reason) {
		this.reason.set(reason);
		this.failed.set(true);
	}

	@Override
	public String errorText() {
		return reason.get();
	}

	@Override
	public boolean failed() {
		return failed.get();
	}

	//////////////////////////// cancel/////////////////////////

	@Override
	public void notifyOnCancel(RpcCallback<Object> callback) {
		this.callback.set(callback);
	}

	@Override
	public void startCancel() {
		this.canceled.set(true);
	}

	@Override
	public boolean isCanceled() {
		return canceled.get();
	}

	///////////////////////////////// reset///////////////////////
	@Override
	public void reset() {
		reason.set(null);
		failed.set(false);
		canceled.set(false);
	}
}
