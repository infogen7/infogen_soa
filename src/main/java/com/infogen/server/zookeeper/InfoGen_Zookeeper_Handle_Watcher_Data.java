/**
 * 
 */
package com.infogen.server.zookeeper;

/**
 * 节点数据改变事件处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:30:34
 * @since 1.0
 * @version 1.0
 */
public interface InfoGen_Zookeeper_Handle_Watcher_Data {
	public abstract void handle_event(String name);
}
