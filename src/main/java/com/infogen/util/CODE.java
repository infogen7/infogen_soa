package com.infogen.util;

/**
 * 返回值错误码
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年4月2日 下午12:34:23
 * @since 1.0
 * @version 1.0
 */
public enum CODE {
	_200(200, "成功"), //
	_400(400, "参数不正确"), //
	_401(401, "特定参数不符合条件(eg:没有这个用户)"), //
	_402(402, "没有这个服务"), //
	_403(403, "没有可用的服务节点"), //
	_404(404, "没有这个方法 (RPC调用)"), //
	_500(500, "执行错误"), //
	_501(501, "权限验证失败,不在白名单内"), //
	_502(502, "调用超时"), //
	_510(510, "处理返回值错误");
	public String note;
	public Integer code;

	private CODE(Integer code, String note) {
		this.note = note;
		this.code = code;
	}
}
