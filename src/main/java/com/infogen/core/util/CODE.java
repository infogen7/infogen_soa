package com.infogen.core.util;

/**
 * infogen框架的返回值错误码
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:34:23
 * @since 1.0
 * @version 1.0
 */
public enum CODE {
	success(200, "成功"), //

	parameters_incorrect(400, "参数不正确"), //
	parameters_invalid(401, "特定参数不符合条件(eg:没有这个用户)"), //
	service_notfound(402, "没有这个服务"), //
	node_unavailable(403, "没有可用的服务节点"), //

	error(500, "执行错误"), //
	authentication_fail(501, "认证失败"), //
	roles_fail(502, "授权失败"), //
	session_expiration(503, "Session 过期"), //
	session_lose(504, "Session 丢失"), //

	timeout(510, "调用超时"), //
	generate_return_error(511, "处理返回值错误"), //
	limit(512, "接口调用次数超过限制"), //
	limit_by_group(513, "用户调用次数超过限制");
	public String note;
	public Integer code;

	private CODE(Integer code, String note) {
		this.note = note;
		this.code = code;
	}
}
