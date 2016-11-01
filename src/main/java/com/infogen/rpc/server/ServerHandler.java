package com.infogen.rpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.infogen.core.structure.DefaultEntry;
import com.infogen.rpc.InfoGen_Controller;
import com.infogen.rpc.filter.InfoGen_Filter;
import com.infogen.rpc.header.X_HttpHeaderNames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static final Map<String, DefaultEntry<BlockingService, ExtensionRegistry>> blockingServiceMap = new ConcurrentHashMap<>();
	private static final List<InfoGen_Filter> filter_list = new ArrayList<>();

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	private FullHttpResponse createEmptyHttpResponse(FullHttpRequest request, HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		return response;
	}

	private void flush(ChannelHandlerContext ctx, Boolean keepAlive, FullHttpRequest request,
			FullHttpResponse response) {
		response.headers().setInt(X_HttpHeaderNames.x_sequence.key,
				request.headers().getInt(X_HttpHeaderNames.x_sequence.key, 0));
		if (keepAlive) {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			ctx.write(response);
		} else {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		Boolean keepAlive = HttpUtil.isKeepAlive(request);
		// 异步返回必须知道该序列号才能返回调用程序结果
		if (HttpUtil.is100ContinueExpected(request)) {
			// 通常要POST的数据大于1024字节的时候,会分为俩步,
			// 1. 发送一个请求, 包含一个Expect:100-continue, 询问Server使用愿意接受数据
			// 2. 接收到Server返回的100-continue应答以后, 才把数据POST给Server
			flush(ctx, keepAlive, request, createEmptyHttpResponse(request, HttpResponseStatus.CONTINUE));
			return;
		}
		// 参数是否符合规范
		String[] split = request.uri().split("/");
		if (split.length < 3) {
			flush(ctx, keepAlive, request, createEmptyHttpResponse(request, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		// 是否存在该请求地址
		String service_name = split[1];
		String method_name = split[2];
		Entry<BlockingService, ExtensionRegistry> pair = blockingServiceMap.get(service_name);
		if (pair == null) {
			flush(ctx, keepAlive, request, createEmptyHttpResponse(request, HttpResponseStatus.NOT_FOUND));
			return;
		}

		FullHttpResponse response = createEmptyHttpResponse(request, HttpResponseStatus.OK);
		// filter
		for (InfoGen_Filter filter : filter_list) {
			if (!filter.doFilter(ctx, request, response)) {
				flush(ctx, keepAlive, request, response);
				return;
			}
		}

		final BlockingService blockingService = pair.getKey();
		final ExtensionRegistry registry = pair.getValue();

		ByteBuf buf = request.content();
		byte[] resp = new byte[buf.readableBytes()];
		buf.readBytes(resp);
		InfoGen_Controller controller = new InfoGen_Controller();

		final MethodDescriptor methodDescriptor = blockingService.getDescriptorForType().findMethodByName(method_name);
		Message methodRequest = blockingService.getRequestPrototype(methodDescriptor).newBuilderForType()
				.mergeFrom(resp, registry).build();
		Message callBlockingMethod = blockingService.callBlockingMethod(methodDescriptor, controller, methodRequest);

		response.content().writeBytes(callBlockingMethod.toByteArray());
		response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

		String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
			if (!cookies.isEmpty()) {
				for (Cookie cookie : cookies) {
					response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
				}
			}
		}
		flush(ctx, keepAlive, request, response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public static void add_filter(final InfoGen_Filter filter) {
		filter_list.add(filter);
	}

	public static void registerService(final BlockingService service) {
		if (blockingServiceMap.containsKey(service.getDescriptorForType().getFullName())) {
			throw new IllegalArgumentException("BlockingService already registered");
		}
		final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
		service.getDescriptorForType().getFile().getExtensions().forEach(extensionRegistry::add);
		blockingServiceMap.put(service.getDescriptorForType().getFullName(),
				new DefaultEntry<BlockingService, ExtensionRegistry>(service, extensionRegistry));
	}

}