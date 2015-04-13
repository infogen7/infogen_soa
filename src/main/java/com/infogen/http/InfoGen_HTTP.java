/**
 * 
 */
package com.infogen.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.infogen.http.callback.Http_Callback;
import com.infogen.util.CODE;
import com.infogen.util.Return;

/**
 * http调用的工具类
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月30日 下午1:05:24
 */
public class InfoGen_HTTP {
	public static final Logger logger = Logger.getLogger(InfoGen_HTTP.class.getName());
	// 当使用长轮循时需要注意不能超过此时间
	private static Integer socket_timeout = 10_000;// 数据传输时间
	private static Integer connect_timeout = 3_000;// 连接时间

	// ////////////////////////////////////////////////////////post///////////////////////////////////////////////////////////////////////////
	/**
	 * post 获取 rest 资源
	 * 
	 * @param url
	 * @param name_value_pair
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Return do_post(String url, List<BasicNameValuePair> name_value_pair) throws IOException {
		String body = "{}";
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socket_timeout).setConnectTimeout(connect_timeout).build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		try {
			HttpPost httpost = new HttpPost(url);
			httpost.setEntity(new UrlEncodedFormEntity(name_value_pair, StandardCharsets.UTF_8));
			HttpResponse response = httpclient.execute(httpost);
			HttpEntity entity = response.getEntity();
			body = EntityUtils.toString(entity);
		} finally {
			httpclient.close();
		}
		return Return.create(body);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////get/////////////////////////////////////////////////////////////
	/**
	 * get 获取 rest 资源
	 * 
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Return do_get(String url, List<BasicNameValuePair> name_value_pair) throws ClientProtocolException, IOException {
		String body = "{}";
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socket_timeout).setConnectTimeout(connect_timeout).build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		try {
			if (name_value_pair != null) {
				StringBuffer do_get_sbf = new StringBuffer();
				do_get_sbf.append(url).append("?");
				for (int j = 0; j < name_value_pair.size(); j++) {
					if (j != 0) {
						do_get_sbf.append("&");
					}
					NameValuePair nameValuePair = name_value_pair.get(j);
					do_get_sbf.append(nameValuePair.getName()).append("=").append(nameValuePair.getValue());
				}
				url = do_get_sbf.toString();
			}
			HttpResponse response = httpclient.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();
			body = EntityUtils.toString(entity);
		} finally {
			httpclient.close();
		}
		return Return.create(body);
	}

	// ///////////////////////////////////////////async//////////////////////////////////////////////////
	public static Http_Callback do_async_post(String url, List<BasicNameValuePair> name_value_pair) throws IOException {
		Http_Callback callback = new Http_Callback();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socket_timeout).setConnectTimeout(connect_timeout).build();
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		httpclient.start();
		HttpPost httpost = new HttpPost(url);
		httpost.setEntity(new UrlEncodedFormEntity(name_value_pair, StandardCharsets.UTF_8));
		httpclient.execute(httpost, new FutureCallback<HttpResponse>() {
			public void completed(final HttpResponse response) {
				try {
					callback.add(Return.create(EntityUtils.toString(response.getEntity())));
				} catch (ParseException | IOException e) {
					callback.add(Return.FAIL(CODE._500.code, e.getMessage()));
				}
			}

			public void failed(final Exception e) {
				callback.add(Return.FAIL(CODE._500.code, e.getMessage()));
			}

			public void cancelled() {
				callback.add(Return.FAIL(CODE._500.code, "cancelled"));
			}
		});
		return callback;
	}

	public static Http_Callback do_async_get(String url, List<BasicNameValuePair> name_value_pair) throws IOException {

		Http_Callback callback = new Http_Callback();

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socket_timeout).setConnectTimeout(connect_timeout).build();
		CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
		if (name_value_pair != null) {
			StringBuffer do_async_get_sbf = new StringBuffer();
			do_async_get_sbf.append(url).append("?");
			for (int j = 0; j < name_value_pair.size(); j++) {
				if (j != 0) {
					do_async_get_sbf.append("&");
				}
				NameValuePair nameValuePair = name_value_pair.get(j);
				do_async_get_sbf.append(nameValuePair.getName()).append("=").append(nameValuePair.getValue());
			}
			url = do_async_get_sbf.toString();
		}

		httpclient.start();
		HttpGet httpget = new HttpGet(url);
		httpclient.execute(httpget, new FutureCallback<HttpResponse>() {
			public void completed(final HttpResponse response) {
				try {
					callback.add(Return.create(EntityUtils.toString(response.getEntity())));
				} catch (ParseException | IOException e) {
					callback.add(Return.FAIL(CODE._500.code, e.getMessage()));
				}
			}

			public void failed(final Exception e) {
				callback.add(Return.FAIL(CODE._500.code, e.getMessage()));
			}

			public void cancelled() {
				callback.add(Return.FAIL(CODE._500.code, "cancelled"));
			}
		});
		return callback;
	}
}
