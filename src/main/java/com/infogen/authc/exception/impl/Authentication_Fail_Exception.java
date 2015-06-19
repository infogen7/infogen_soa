package com.infogen.authc.exception.impl;

import com.infogen.authc.exception.InfoGen_Auth_Exception;
import com.larrylgq.aop.util.CODE;

/**
 * 认证失败的异常
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月11日 上午11:43:15
 * @since 1.0
 * @version 1.0
 */
public class Authentication_Fail_Exception extends InfoGen_Auth_Exception {
	private static final long serialVersionUID = 73928252582564139L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#code()
	 */
	@Override
	public Integer code() {
		// TODO Auto-generated method stub
		return CODE.authentication_fail.code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#name()
	 */
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return CODE.authentication_fail.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#note()
	 */
	@Override
	public String note() {
		// TODO Auto-generated method stub
		return CODE.authentication_fail.note;
	}

}
