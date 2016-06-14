package com.infogen.http_client.exception;

/**
 * response.isSuccessful()为false的错误
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月11日 上午11:44:55
 * @since 1.0
 * @version 1.0
 */
public class HTTP_Fail_Exception extends Exception {
	private static final long serialVersionUID = 153970941852883330L;
	private Integer code;
	private String message;

	public HTTP_Fail_Exception(Integer code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return "HTTP调用时response.isSuccessful()出现为false的错误 code:".concat(code.toString()).concat(" - message:".concat(message));
	}

}
