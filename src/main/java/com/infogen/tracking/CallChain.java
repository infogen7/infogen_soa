package com.infogen.tracking;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月8日 下午12:24:19
 * @since 1.0
 * @version 1.0
 */
public class CallChain {
	// cookie等用户标识,*sessionid(token),*客户端类型 ,traceid,sequence,来源地址 ,来源ip,当前地址,当前ip,当前服务 ,调用时间 ,调用时长,调用状态 ,数据大小
	// a00000... ,t0000,测试/京东/聚信立,tr00000,0 ,home.html ,xx ,send ,xx ,中控 ,2015050X ,300ms ,ok/error/auth,1.3k
	private String trackid;
	private String identify;// cookie等用户标识
	private String sessionid = "";// session id
	private Integer sequence;
	private String referer = "";

	private String referer_ip;
	private String target;
	private String target_ip;
	private String target_server;

	public String getIdentify() {
		return identify;
	}

	public void setIdentify(String identify) {
		this.identify = identify;
	}

	public String getTrackid() {
		return trackid;
	}

	public void setTrackid(String trackid) {
		this.trackid = trackid;
	}

	public String getSessionid() {
		return sessionid;
	}

	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getReferer_ip() {
		return referer_ip;
	}

	public void setReferer_ip(String referer_ip) {
		this.referer_ip = referer_ip;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTarget_ip() {
		return target_ip;
	}

	public void setTarget_ip(String target_ip) {
		this.target_ip = target_ip;
	}

	public String getTarget_server() {
		return target_server;
	}

	public void setTarget_server(String target_server) {
		this.target_server = target_server;
	}

}
