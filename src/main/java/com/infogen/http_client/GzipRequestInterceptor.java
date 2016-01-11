package com.infogen.http_client;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * TODO WARN util.UrlEncoded : org.eclipse.jetty.util.Utf8Appendable$NotUtf8Exception: Not valid UTF8! byte 8b in state 0
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:47:51
 * @since 1.0
 * @version 1.0
 */
public class GzipRequestInterceptor implements okhttp3.Interceptor {
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		if (originalRequest.body() == null || originalRequest.header("Content-Encoding") != null) {
			return chain.proceed(originalRequest);
		}

		Request compressedRequest = originalRequest.newBuilder().header("Content-Encoding", "gzip").method(originalRequest.method(), gzip(originalRequest.body())).build();
		return chain.proceed(compressedRequest);
	}

	private RequestBody gzip(final RequestBody body) {
		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return body.contentType();
			}

			@Override
			public long contentLength() {
				return -1; // We don't know the compressed length in advance!
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
				body.writeTo(gzipSink);
				gzipSink.close();
			}
		};
	}

}