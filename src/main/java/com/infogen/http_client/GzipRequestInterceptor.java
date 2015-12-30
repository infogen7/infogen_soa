package com.infogen.http_client;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import okio.Buffer;
import okio.BufferedSink;
import okio.ByteString;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年11月20日 下午6:47:51
 * @since 1.0
 * @version 1.0
 */
public class GzipRequestInterceptor implements Interceptor {
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		if (originalRequest.body() == null || originalRequest.header("Content-Type") == null) {
			return chain.proceed(originalRequest);
		}
		if (originalRequest.header("Content-Type").equals(MultipartBuilder.FORM)) {
			Request compressedRequest = originalRequest.newBuilder().header("Content-Encoding", "gzip").method(originalRequest.method(), gzip(originalRequest.body())).build();
			return chain.proceed(compressedRequest);
		}
		return chain.proceed(originalRequest);
	}

	private RequestBody gzip(final RequestBody body) throws IOException {
		final Buffer inputBuffer = new Buffer();
		body.writeTo(inputBuffer);

		@SuppressWarnings("resource")
		final Buffer outputBuffer = new Buffer();
		GZIPOutputStream gos = new GZIPOutputStream(outputBuffer.outputStream());

		gos.write(inputBuffer.readByteArray());

		inputBuffer.close();
		gos.close();

		return new RequestBody() {
			@Override
			public MediaType contentType() {
				return body.contentType();
			}

			@Override
			public long contentLength() {
				return outputBuffer.size();
			}

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
				ByteString snapshot = outputBuffer.snapshot();
				sink.write(snapshot);
			}
		};
	}
}