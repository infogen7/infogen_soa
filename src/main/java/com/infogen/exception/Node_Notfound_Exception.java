package com.infogen.exception;

import com.infogen.util.CODE;

/**
 * 依赖服务没有找到任何可用节点的异常
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月18日 下午4:51:31
 * @since 1.0
 * @version 1.0
 */
public class Node_Notfound_Exception extends InfoGen_Exception {
	private static final long serialVersionUID = -7021908273339016544L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.exception.InfoGen_RPC_Exception#code()
	 */
	@Override
	public Integer code() {
		return CODE.node_notfound.code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.exception.InfoGen_RPC_Exception#name()
	 */
	@Override
	public String name() {
		return CODE.node_notfound.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.exception.InfoGen_RPC_Exception#note()
	 */
	@Override
	public String note() {
		return CODE.node_notfound.note;
	}

}
