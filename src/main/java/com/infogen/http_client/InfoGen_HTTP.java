package com.infogen.http_client;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.http_client.exception.HTTP_Fail_Exception;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * http调用的工具类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:08:13
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_HTTP {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_HTTP.class.getName());
	// 当使用长轮循时需要注意不能超过此时间
	public static Integer connect_timeout = 3_000;// 连接时间
	public static Integer socket_timeout = 30_000;// 数据传输时间
	private static OkHttpClient client = new OkHttpClient.Builder()//
			// TODO WARN util.UrlEncoded : org.eclipse.jetty.util.Utf8Appendable$NotUtf8Exception: Not valid UTF8! byte 8b in state 0
			// .addInterceptor(new GzipRequestInterceptor())
			.connectTimeout(connect_timeout, TimeUnit.MILLISECONDS).writeTimeout(socket_timeout, TimeUnit.MILLISECONDS).readTimeout(socket_timeout, TimeUnit.MILLISECONDS).build();

	public static void set_timeout(Integer connect_timeout, Integer socket_timeout) {
		client = new OkHttpClient.Builder()//
				// TODO WARN util.UrlEncoded : org.eclipse.jetty.util.Utf8Appendable$NotUtf8Exception: Not valid UTF8! byte 8b in state 0
				// .addInterceptor(new GzipRequestInterceptor())
				.connectTimeout(connect_timeout, TimeUnit.MILLISECONDS).writeTimeout(socket_timeout, TimeUnit.MILLISECONDS).readTimeout(socket_timeout, TimeUnit.MILLISECONDS).build();
	}

	static {
		LOGGER.info("初始化HTTP CLIENT");
	}

	// /////////////////////////////////////////////////////////////////get/////////////////////////////////////////////////////////////
	// get 获取 rest 资源
	public static String do_get(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException, HTTP_Fail_Exception {
		Builder builder = new Request.Builder().url(concat_url_params(url, params));
		add_headers(builder, headers);
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static void do_get_async(String url, Map<String, Object> params, Callback callback, Map<String, Object> headers) {
		Builder builder = new Request.Builder().url(concat_url_params(url, params));
		add_headers(builder, headers);
		Request request = builder.build();
		if (callback == null) {
			callback = async_get_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	// ////////////////////////////////////////////////////////post: form json xml///////////////////////////////////////////////////////////////////////////

	public static String do_post(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException, HTTP_Fail_Exception {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
		add_headers(builder, headers);
		okhttp3.FormBody.Builder form_builder = new FormBody.Builder();
		for (Entry<String, Object> entry : params.entrySet()) {
			form_builder.add(entry.getKey(), String.valueOf(entry.getValue()));
		}
		Request request = builder.post(form_builder.build()).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static void do_post_async(String url, Map<String, Object> params, Callback callback, Map<String, Object> headers) throws IOException {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		okhttp3.FormBody.Builder form_builder = new FormBody.Builder();
		for (Entry<String, Object> entry : params.entrySet()) {
			form_builder.add(entry.getKey(), String.valueOf(entry.getValue()));
		}
		Request request = builder.post(form_builder.build()).build();
		if (callback == null) {
			callback = async_post_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	/////////////////////////////////////////////////////////////////////////////////
	public static final okhttp3.MediaType MEDIA_TYPE_JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");//

	public static String do_post_json(String url, String json, Map<String, Object> headers) throws IOException, HTTP_Fail_Exception {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(RequestBody.create(json, MEDIA_TYPE_JSON)).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static void do_post_json_async(String url, String json, Callback callback, Map<String, Object> headers) throws IOException {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(RequestBody.create(json, MEDIA_TYPE_JSON)).build();
		if (callback == null) {
			callback = async_post_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	//
	public static final okhttp3.MediaType MEDIA_TYPE_XML = okhttp3.MediaType.parse("text/xml;charset=UTF-8");//

	public static String do_post_xml(String url, String xml, Map<String, Object> headers) throws IOException, HTTP_Fail_Exception {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(RequestBody.create(xml, MEDIA_TYPE_XML)).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static void do_post_xml_async(String url, String xml, Callback callback, Map<String, Object> headers) throws IOException {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder, headers);
		Request request = builder.post(RequestBody.create(xml, MEDIA_TYPE_XML)).build();
		if (callback == null) {
			callback = async_post_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	///////////////////////////////////////////////////////////////////////////////////////
	private static String concat_url_params(String url, Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			return url;
		}
		Iterator<String> iterator = params.keySet().iterator();
		StringBuilder do_get_sbf = new StringBuilder();

		String first_key = iterator.next();
		do_get_sbf.append(url).append("?").append(first_key).append("=").append(params.get(first_key));
		while (iterator.hasNext()) {
			String key = iterator.next();
			do_get_sbf.append("&").append(key).append("=").append(params.get(key));
		}
		url = do_get_sbf.toString();
		return url;
	}

	private static void add_headers(okhttp3.Request.Builder builder, Map<String, Object> headers) {
		if (headers == null) {
			return;
		}
		headers.forEach((key, value) -> {
			if (value != null) {
				builder.header(key, String.valueOf(value));
			}
		});
	}

	private static final Callback async_post_callback = new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
			Request request = call.request();
			LOGGER.error("do_async_post_bytype 报错:".concat(request.url().toString()), e);
		}

		@Override
		public void onResponse(Call call, Response response) throws IOException {
			if (response.isSuccessful()) {
			} else {
				LOGGER.error("do_async_post_bytype 错误-返回非2xx:".concat(response.request().url().toString()));
			}
		}
	};

	private static final Callback async_get_callback = new Callback() {
		@Override
		public void onFailure(Call call, IOException e) {
			Request request = call.request();
			LOGGER.error("do_async_get 报错:".concat(request.url().toString()), e);
		}

		@Override
		public void onResponse(Call call, Response response) throws IOException {
			if (response.isSuccessful()) {
			} else {
				LOGGER.error("do_async_get 错误-返回非2xx:".concat(response.request().url().toString()));
			}
		}
	};
}
