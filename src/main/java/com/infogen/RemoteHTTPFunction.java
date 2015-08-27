package com.infogen;

import java.util.Map;

import com.infogen.core.util.Return;
import com.infogen.exception.Node_Notfound_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.squareup.okhttp.Callback;

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
	 * @throws Node_Notfound_Exception
	 * @throws Service_Notfound_Exception
	 */
	public void get_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Notfound_Exception {
		service.get_async(url, name_value_pair, callback);
	}

	public void get_async(Map<String, String> name_value_pair, Callback callback, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		service.get_async(url, name_value_pair, callback, seed);
	}

	/**
	 * 异步post调用
	 * 
	 * @param url
	 * @param map
	 * @return
	 * @throws Node_Notfound_Exception
	 * @throws Service_Notfound_Exception
	 */
	public void post_async(Map<String, String> name_value_pair, Callback callback) throws Service_Notfound_Exception, Node_Notfound_Exception {
		service.post_async(url, name_value_pair, callback);
	}

	public void post_async(String url, Map<String, String> name_value_pair, Callback callback, String seed) throws Service_Notfound_Exception, Node_Notfound_Exception {
		service.post_async(url, name_value_pair, callback, seed);
	}

}
