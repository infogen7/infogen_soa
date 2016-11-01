package com.infogen.rpc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月26日 下午6:58:32
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Controller implements RpcController {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Controller.class.getName());
	private final AtomicReference<String> reason = new AtomicReference<>();
	private final AtomicBoolean failed = new AtomicBoolean();
	private final AtomicBoolean canceled = new AtomicBoolean();

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
	@Deprecated
	public void notifyOnCancel(RpcCallback<Object> callback) {
		LOGGER.error("非 Future 方式执行 不能取消执行的线程");
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
