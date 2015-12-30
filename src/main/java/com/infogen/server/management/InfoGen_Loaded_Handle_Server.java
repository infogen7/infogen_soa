/**
 * 
 */
package com.infogen.server.management;

import com.infogen.server.model.RemoteServer;

/**
 * 自定义服务节点数据加载完毕后处理方式的接口
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:25:16
 * @since 1.0
 * @version 1.0
 */
public interface InfoGen_Loaded_Handle_Server {
	public abstract void handle_event(RemoteServer native_server);
}
