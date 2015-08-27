package com.infogen.rpc.client;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月27日 下午6:06:54
 * @since 1.0
 * @version 1.0
 */
public class InfogenFullHttpResponse {
	private HttpHeaders headers;
	private byte[] resp;

	public InfogenFullHttpResponse(HttpHeaders headers, byte[] resp) {
		super();
		this.headers = headers;
		this.resp = resp;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public byte[] getResp() {
		return resp;
	}

	public void setResp(byte[] resp) {
		this.resp = resp;
	}

}
