/**
 * 
 */
package com.infogen.cache.event_handle;

import com.infogen.server.model.NativeServer;

/**
 * 服务节点数据加载完毕处理器
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月30日 下午4:42:29
 */
public interface InfoGen_Loaded_Handle_Server {
	public abstract void handle_event(NativeServer native_server);
}
