package com.infogen.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.http.callback.Http_Callback;
import com.infogen.http.exception.HTTP_Fail_Exception;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.tracking.enum0.Track_Header;
import com.infogen.util.CODE;
import com.infogen.util.Return;
import com.larrylgq.aop.tools.Tool_Jackson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * http调用的工具类
 * 
 * @author larry
 * @email larrylv@outlook.com
 * @version 创建时间 2014年10月30日 下午1:05:24
 */
public class InfoGen_HTTP {
	private static final Logger LOGGER = Logger.getLogger(InfoGen_HTTP.class.getName());
	// 当使用长轮循时需要注意不能超过此时间
	public static Integer socket_timeout = 30_000;// 数据传输时间
	public static Integer connect_timeout = 3_000;// 连接时间
	private static final OkHttpClient client = new OkHttpClient();
	static {
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
			String sessionid = callChain.getSessionid();
			if (sessionid != null) {
				builder.header(Track_Header.x_session_id.key, sessionid);
			}
			builder.header(Track_Header.x_track_id.key, callChain.getTrackid())//
					.header(Track_Header.x_identify.key, callChain.getIdentify())//
					.header(Track_Header.x_sequence.key, callChain.getSequence().toString())//
					.header(Track_Header.x_referer.key, callChain.getTarget());//
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
		url = concat_url_params(url, params);
		Builder builder = new Request.Builder().url(url);
		add_headers(builder);
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static Http_Callback do_async_get(String url, Map<String, String> params) {
		Http_Callback callback = new Http_Callback();
		url = concat_url_params(url, params);

		Builder builder = new Request.Builder().url(url);
		add_headers(builder);
		Request request = builder.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				callback.add(Return.FAIL(CODE.error, e).toJson());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					callback.add(response.body().string());
				} else {
					callback.add(Return.FAIL(CODE.error).toJson());
				}
			}
		});
		return callback;
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
			return response.body().string();
		} else {
			throw new HTTP_Fail_Exception(response.code(), response.message());
		}
	}

	public static Http_Callback do_async_post(String url, Map<String, String> params) throws IOException {
		return do_async_post_bytype(url, MEDIA_TYPE_FORM, concat_params(params));
	}

	public static Http_Callback do_async_post_json(String url, Map<String, String> params) throws IOException {
		return do_async_post_bytype(url, MEDIA_TYPE_JSON, Tool_Jackson.toJson(params));
	}

	private static Http_Callback do_async_post_bytype(String url, MediaType type, String params) throws IOException {
		Http_Callback callback = new Http_Callback();
		Builder builder = new Request.Builder().url(url);
		add_headers(builder);
		Request request = builder.post(RequestBody.create(type, params)).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				callback.add(Return.FAIL(CODE.error, e).toJson());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					callback.add(response.body().string());
				} else {
					callback.add(Return.FAIL(CODE.error).toJson());
				}
			}
		});
		return callback;
	}
}
