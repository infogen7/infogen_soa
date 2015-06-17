/**
 * 
 */
package com.infogen.cache;

/**
 * 配置节点数据加载完毕处理器
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月30日 下午4:42:29
 */
public interface InfoGen_Loaded_Handle_Configuration {
	public abstract void handle_event(String data);
}
