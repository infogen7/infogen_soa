package com.infogen.authc.exception.impl;

import com.infogen.authc.exception.InfoGen_Auth_Exception;

/**
 * 没有这个会话
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月15日 下午12:30:39
 * @since 1.0
 * @version 1.0
 */
public class Session_Lose_Exception extends InfoGen_Auth_Exception {

	private static final long serialVersionUID = -3944897882402426587L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#code()
	 */
	@Override
	public Integer code() {
		// TODO Auto-generated method stub
		return Auth_Exception_CODE.session_lose.code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#name()
	 */
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return Auth_Exception_CODE.session_lose.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#note()
	 */
	@Override
	public String note() {
		// TODO Auto-generated method stub
		return Auth_Exception_CODE.session_lose.note;
	}

}
