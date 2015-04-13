/**
 * 
 */
package com.infogen.event_handle;

import org.apache.log4j.Logger;

/**
 * zookeeper session 失效处理器
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年12月30日 下午5:14:18
 */
public interface Zookeeper_Expired_Handle {
	public static final Logger logger = Logger.getLogger(Zookeeper_Expired_Handle.class.getName());

	public abstract void handle_event() throws Exception;
}
