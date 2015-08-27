package com.infogen.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.core.tools.Tool_Jackson;
import com.infogen.exception.HTTP_Fail_Exception;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.util.InfoGen_Header;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * http调用的工具类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月3日 上午11:08:13
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_HTTP {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_HTTP.class.getName());
	// 当使用长轮循时需要注意不能超过此时间
	public static Integer socket_timeout = 30_000;// 数据传输时间
	public static Integer connect_timeout = 3_000;// 连接时间
	private static final OkHttpClient client = new OkHttpClient();

	static {
		List<Protocol> list = new ArrayList<>();
		list.add(Protocol.HTTP_2);
		list.add(Protocol.HTTP_1_1);
		client.setProtocols(list);
		client.setConnectTimeout(connect_timeout, TimeUnit.MILLISECONDS);
		client.setReadTimeout(socket_timeout, TimeUnit.MILLISECONDS);
		client.setWriteTimeout(socket_timeout, TimeUnit.MILLISECONDS);
		LOGGER.info("初始化HTTP CLIENT");
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////get/////////////////////////////////////////////////////////////
	private static String concat_url_params(String url, Map<String, String> params) {
		if (params == null) {
			params = new HashMap<>();
		}
		StringBuilder do_get_sbf = new StringBuilder();
		do_get_sbf.append(url);
		String[] keys = new String[params.size()];
		params.keySet().toArray(keys);
		for (int j = 0; j < keys.length; j++) {
			if (j == 0) {
				do_get_sbf.append("?");
			} else {
				do_get_sbf.append("&");
			}
			String key = keys[j];
			String value = params.get(key);
			do_get_sbf.append(key).append("=").append(value);
		}
		url = do_get_sbf.toString();
		return url;
	}

	private static void add_headers(Builder builder) {
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();
		if (callChain != null) {
			// 注意:builder.header 不能写入空值,会报异常
			String sessionid = callChain.getSessionid();
			if (sessionid != null) {
				builder.header(InfoGen_Header.x_session_id.key, sessionid);
			}
			String referer = callChain.getReferer();
			if (referer != null) {
				builder.header(InfoGen_Header.x_referer.key, callChain.getTarget());//
			}
			String trackid = callChain.getTrackid();
			if (trackid != null) {
				builder.header(InfoGen_Header.x_track_id.key, callChain.getTrackid())//
						.header(InfoGen_Header.x_identify.key, callChain.getIdentify())//
						.header(InfoGen_Header.x_sequence.key, callChain.getSequence().toString());
			}
		}
	}

	/**
	 * get 获取 rest 资源
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String do_get(String url, Map<String, String> params) throws IOException {
		Builder builder = new Request.Builder().url(concat_url_params(url, params));
		add_headers(builder);
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			LOGGER.info("OkHttp-Selected-Protocol: " + response.header("OkHttp-Selected-Protocol"));
			LOGGER.info("Response code is " + response.code());
			return response.body().string();
		} else {
			LOGGER.info("OkHttp-Selected-Protocol: " + response.header("OkHttp-Selected-Protocol"));
			LOGGER.info("Response code is " + response.code());
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	private static final Callback async_get_callback = new Callback() {
		@Override
		public void onFailure(Request request, IOException e) {
			LOGGER.error("do_async_get 报错:".concat(request.urlString()), e);
		}

		@Override
		public void onResponse(Response response) throws IOException {
			if (response.isSuccessful()) {
			} else {
				LOGGER.error("do_async_get 错误-返回非2xx:".concat(response.request().urlString()));
			}
		}
	};

	public static void do_async_get(String url, Map<String, String> params, Callback callback) {
		Builder builder = new Request.Builder().url(concat_url_params(url, params));
		add_headers(builder);
		Request request = builder.build();
		if (callback == null) {
			callback = async_get_callback;
		}
		client.newCall(request).enqueue(callback);
	}

	// ////////////////////////////////////////////////////////post///////////////////////////////////////////////////////////////////////////
	private static String concat_params(Map<String, String> params) {
		if (params == null) {
			params = new HashMap<>();
		}
		StringBuilder do_get_sbf = new StringBuilder();
		String[] keys = new String[params.size()];
		params.keySet().toArray(keys);
		for (int j = 0; j < keys.length; j++) {
			if (j != 0) {
				do_get_sbf.append("&");
			}
			String key = keys[j];
			String value = params.get(key);
			do_get_sbf.append(key).append("=").append(value);
		}
		return do_get_sbf.toString();
	}

	public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");//
	public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
	public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");//
	public static final MediaType MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//

	public static String do_post(String url, Map<String, String> params) throws IOException {
		return do_post_bytype(url, MEDIA_TYPE_FORM, concat_params(params));
	}

	public static String do_post_json(String url, Map<String, String> params) throws IOException {
		return do_post_bytype(url, MEDIA_TYPE_JSON, Tool_Jackson.toJson(params));
	}

	private static String do_post_bytype(String url, MediaType type, String params) throws IOException {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder);
		Request request = builder.post(RequestBody.create(type, params)).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			LOGGER.info("OkHttp-Selected-Protocol: " + response.header("OkHttp-Selected-Protocol"));
			LOGGER.info("Response code is " + response.code());
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static void do_async_post(String url, Map<String, String> params, Callback callback) throws IOException {
		do_async_post_bytype(url, MEDIA_TYPE_FORM, concat_params(params), callback);
	}

	public static void do_async_post_json(String url, Map<String, String> params, Callback callback) throws IOException {
		do_async_post_bytype(url, MEDIA_TYPE_JSON, Tool_Jackson.toJson(params), callback);
	}

	private static final Callback async_post_callback = new Callback() {
		@Override
		public void onFailure(Request request, IOException e) {
			LOGGER.error("do_async_post_bytype 报错:".concat(request.urlString()), e);
		}

		@Override
		public void onResponse(Response response) throws IOException {
			if (response.isSuccessful()) {
			} else {
				LOGGER.error("do_async_post_bytype 错误-返回非2xx:".concat(response.request().urlString()));
			}
		}
	};

	private static void do_async_post_bytype(String url, MediaType type, String params, Callback callback) throws IOException {
		Builder builder = new Request.Builder().url(url);
		add_headers(builder);
		Request request = builder.post(RequestBody.create(type, params)).build();
		if (callback == null) {
			callback = async_post_callback;
		}
		client.newCall(request).enqueue(callback);
	}
}
