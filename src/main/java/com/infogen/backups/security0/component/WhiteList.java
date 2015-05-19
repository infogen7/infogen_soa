package com.infogen.backups.security0.component;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月7日 下午6:16:04
 * @since 1.0
 * @version 1.0
 */
public class WhiteList {
	private String url;
	private String ip;

	public WhiteList() {
	}

	public WhiteList(String url, String ip) {
		this.url = url;
		this.ip = ip;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
