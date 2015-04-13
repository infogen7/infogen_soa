/**
 * 
 */
package com.infogen.event_handle;


/**
 * 节点数据改变事件处理器
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月30日 下午4:42:29
 */
public interface Zookeeper_Watcher_Data_Handle {
	public abstract void handle_event(String name);
}
