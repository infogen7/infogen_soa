package com.infogen.core.exception;

import com.infogen.core.exception.InfoGen_Exception;
import com.infogen.core.util.CODE;

/**
 * response.isSuccessful()为false的错误
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月11日 上午11:44:55
 * @since 1.0
 * @version 1.0
 */
public class Timeout_Exception extends InfoGen_Exception {
	private static final long serialVersionUID = 7505430625937727751L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.exception.InfoGen_RPC_Exception#code()
	 */
	@Override
	public Integer code() {
		return CODE.timeout.code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.exception.InfoGen_RPC_Exception#name()
	 */
	@Override
	public String name() {
		return CODE.timeout.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.exception.InfoGen_RPC_Exception#note()
	 */
	@Override
	public String note() {
		return CODE.timeout.note;
	}

}
