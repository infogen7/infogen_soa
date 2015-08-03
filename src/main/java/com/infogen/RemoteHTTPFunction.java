package com.infogen;

import java.util.Map;

import com.infogen.http.callback.Http_Callback;
import com.infogen.util.Return;

/**
 * http协议下远程方法的映射
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年7月22日 下午5:28:57
 * @since 1.0
 * @version 1.0
 */
public class RemoteHTTPFunction {
	private Service service;
	private String url;

	public RemoteHTTPFunction(Service service, String url) {
		this.service = service;
		this.url = url;
	}

	/**
	 * 同步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Return get(Map<String, String> name_value_pair) {
		return service.get(url, name_value_pair);
	}

	public Return get(String url, Map<String, String> name_value_pair, String seed) {
		return service.get(url, name_value_pair, seed);
	}

	/**
	 * 同步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Return post(Map<String, String> name_value_pair) {
		return service.post(url, name_value_pair);
	}

	public Return post(Map<String, String> name_value_pair, String seed) {
		return service.post(url, name_value_pair, seed);
	}

	/**
	 * 异步get调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Http_Callback get_async(Map<String, String> name_value_pair) {
		return service.get_async(url, name_value_pair);
	}

	public Http_Callback get_async(Map<String, String> name_value_pair, String seed) {
		return service.get_async(url, name_value_pair, seed);
	}

	/**
	 * 异步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 */
	public Http_Callback post_async(Map<String, String> name_value_pair) {
		return service.post_async(url, name_value_pair);
	}

	public Http_Callback post_async(String url, Map<String, String> name_value_pair, String seed) {
		return service.post_async(url, name_value_pair, seed);
	}

}
