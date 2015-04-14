/**
* @author larry/larrylv@outlook.com
* @date 创建时间 2015年4月13日 下午6:23:30
* @version 1.0
*/
package com.infogen.rpc.handler;

import com.infogen.server.NativeServer;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月13日 下午6:23:30
 * @since 1.0
 * @version 1.0
 */
public interface Thrift_Client_Handler {
	public abstract void handle_event(NativeServer native_server);
}
