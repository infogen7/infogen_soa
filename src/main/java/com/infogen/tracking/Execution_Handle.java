/**
 * 
 */
package com.infogen.tracking;

import com.infogen.tracking.CallChain;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年3月27日 下午4:09:09
 * @since 1.0
 * @version 1.0
 */
public interface Execution_Handle {
	public void insert_after_call_back(String class_name, String method_name, String user_definition, String full_method_name, CallChain callChain, Integer concurrent,Long duration, Object return0);
	public void add_catch_call_back(String class_name, String method_name, String user_definition, String full_method_name, CallChain callChain, Integer concurrent);
}
