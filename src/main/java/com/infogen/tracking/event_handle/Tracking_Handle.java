package com.infogen.tracking.event_handle;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月29日 下午3:35:56
 * @since 1.0
 * @version 1.0
 */
public abstract class Tracking_Handle {

	public abstract void insert_after_call_back(String class_name, String method_name, String user_definition, String full_method_name, long start_millis, long end_millis, Object return0);

	public abstract void add_catch_call_back(String class_name, String method_name, String user_definition, String full_method_name, Throwable e);
}
