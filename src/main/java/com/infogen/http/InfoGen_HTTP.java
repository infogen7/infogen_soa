package com.infogen.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.infogen.http.callback.Http_Callback;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.tracking.enum0.Track;
import com.infogen.util.CODE;
import com.infogen.util.Return;
import com.larrylgq.aop.tools.Tool_Jackson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
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
	public static final Logger logger = Logger.getLogger(InfoGen_HTTP.class.getName());
	// 当使用长轮循时需要注意不能超过此时间
	private static Integer socket_timeout = 10_000;// 数据传输时间
	private static Integer connect_timeout = 3_000;// 连接时间
	private static final OkHttpClient client = new OkHttpClient();
	static {
		client.setConnectTimeout(connect_timeout, TimeUnit.SECONDS);
		client.setReadTimeout(socket_timeout, TimeUnit.SECONDS);
		client.setWriteTimeout(socket_timeout, TimeUnit.SECONDS);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(do_get("http://www.eclipse.org/jetty/documentation/current/http-client-api.html", null));
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////get/////////////////////////////////////////////////////////////
	private static String concat_url_params(String url, Map<String, String> params) {
		if (params == null) {
			params = new HashMap<>();
		}
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();
		if (callChain != null) {
			params.put(Track.x_track_id.key, callChain.getTrackid());
			params.put(Track.x_identify.key, callChain.getIdentify());
			params.put(Track.x_sequence.key, callChain.getSequence().toString());
			params.put(Track.x_referer.key, callChain.getReferer());
		}

		StringBuffer do_get_sbf = new StringBuffer();
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

	/**
	 * get 获取 rest 资源
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String do_get(String url, Map<String, String> params) throws IOException {
		url = concat_url_params(url, params);
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new IOException("Unexpected code " + response);
		}
	}

	public static Http_Callback do_async_get(String url, Map<String, String> params) {
		Http_Callback callback = new Http_Callback();
		url = concat_url_params(url, params);

		Request request = new Request.Builder().url(url).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				callback.add(Return.FAIL(CODE._500, e).toJson());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					callback.add(response.body().string());
				} else {
					callback.add(Return.FAIL(CODE._500).toJson());
				}
			}
		});
		return callback;
	}

	// ////////////////////////////////////////////////////////post///////////////////////////////////////////////////////////////////////////
	public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
	public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
	public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

	public static String do_post(String url, Map<String, String> params) throws IOException {
		if (params == null) {
			params = new HashMap<>();
		}
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();
		if (callChain != null) {
			params.put(Track.x_track_id.key, callChain.getTrackid());
			params.put(Track.x_identify.key, callChain.getIdentify());
			params.put(Track.x_sequence.key, callChain.getSequence().toString());
			params.put(Track.x_referer.key, callChain.getReferer());
		}

		Request request = new Request.Builder().url(url).post(RequestBody.create(MEDIA_TYPE_JSON, Tool_Jackson.toJson(params))).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.body().string();
		} else {
			throw new IOException("Unexpected code " + response);
		}
	}

	public static Http_Callback do_async_post(String url, Map<String, String> params) throws IOException {
		Http_Callback callback = new Http_Callback();
		if (params == null) {
			params = new HashMap<>();
		}
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();
		if (callChain != null) {
			params.put(Track.x_track_id.key, callChain.getTrackid());
			params.put(Track.x_identify.key, callChain.getIdentify());
			params.put(Track.x_sequence.key, callChain.getSequence().toString());
			params.put(Track.x_referer.key, callChain.getReferer());
		}

		Request request = new Request.Builder().url(url).post(RequestBody.create(MEDIA_TYPE_JSON, Tool_Jackson.toJson(params))).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				callback.add(Return.FAIL(CODE._500, e).toJson());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				if (response.isSuccessful()) {
					callback.add(response.body().string());
				} else {
					callback.add(Return.FAIL(CODE._500).toJson());
				}
			}
		});
		return callback;
	}
}
