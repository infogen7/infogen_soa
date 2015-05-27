package com.infogen.authc.exception.impl;

import com.infogen.authc.exception.InfoGen_Auth_Exception;

/**
 * Token 签名错误
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月14日 下午5:20:05
 * @since 1.0
 * @version 1.0
 */
public class Token_Signature_Fail_Exception extends InfoGen_Auth_Exception {
	private static final long serialVersionUID = 2893721728638038400L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#code()
	 */
	@Override
	public Integer code() {
		// TODO Auto-generated method stub
		return Auth_Exception_CODE.token_signature_fail.code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#name()
	 */
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return Auth_Exception_CODE.token_signature_fail.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.authc.exception.InfoGen_Auth_Exception#note()
	 */
	@Override
	public String note() {
		// TODO Auto-generated method stub
		return Auth_Exception_CODE.token_signature_fail.note;
	}

}
