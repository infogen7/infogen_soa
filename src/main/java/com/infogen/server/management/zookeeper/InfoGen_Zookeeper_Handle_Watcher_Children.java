/**
 * 
 */
package com.infogen.server.management.zookeeper;

/**
 * 子节点改变事件处理器
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:30:22
 * @since 1.0
 * @version 1.0
 */
public interface InfoGen_Zookeeper_Handle_Watcher_Children {
	public abstract void handle_event(String path);
}
