package com.infogen.tracking;

import com.infogen.InfoGen;
import com.infogen.configuration.InfoGen_Configuration;

/**
 * 调用链记录类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月8日 下午12:24:19
 * @since 1.0
 * @version 1.0
 */
public class CallChain {
	private InfoGen_Configuration infogen_configuration = InfoGen.getInstance().getInfogen_configuration();
	// 初始化配置时赋值
	// traceid,sequence,来源地址 ,来源ip,当前地址,当前ip,当前服务 ,当前类,当前方法,调用时间 ,调用时长,调用状态(成功/失败) ,返回数据大小,cookie等用户标识,sessionid(token),方法类型(mysql/redis/interface),当前并发数
	// tr00000,0 ,home.html ,xx ,send ,xx ,中控 ,2015050X ,300ms ,ok/error/auth,1.3k ,t0000,测试/京东/聚信立, a00000...
	private String trackid;
	private String identify;// cookie等用户标识
	private String sessionid;// session id(token等会话标识)
	private Integer sequence = 0;
	private String referer;

	private String referer_ip;
	private String target;
	private String target_ip;
	private String target_server;

	public CallChain() {
		// target ip
		setTarget_ip(infogen_configuration.register_node.getIp());
		// target server 当前服务
		setTarget_server(infogen_configuration.register_server.getName());
	}

	public CallChain(String trackid, String identify, String sessionid, Integer sequence, String referer, String referer_ip, String target) {
		this();
		this.trackid = trackid;
		this.identify = identify;
		this.sessionid = sessionid;
		this.sequence = sequence;
		this.referer = referer;
		this.referer_ip = referer_ip;
		this.target = target;
	}

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
