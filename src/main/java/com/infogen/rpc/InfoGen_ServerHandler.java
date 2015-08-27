package com.infogen.rpc;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.infogen.rpc.client.InfoGen_RPCController;
import com.infogen.util.DefaultEntry;
import com.infogen.util.InfoGen_Header;

import io.netty.buffer.ByteBuf;

/**
* @author larry/larrylv@outlook.com/创建时间 2015年8月25日 下午5:57:58
* @since 1.0
* @version 1.0
*/

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

public class InfoGen_ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private static final Map<String, DefaultEntry<BlockingService, ExtensionRegistry>> blockingServiceMap = new ConcurrentHashMap<>();

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		// 通常要POST的数据大于1024字节的时候,会分为俩步,
		// 1. 发送一个请求, 包含一个Expect:100-continue, 询问Server使用愿意接受数据
		// 2. 接收到Server返回的100-continue应答以后, 才把数据POST给Server
		if (HttpHeaderUtil.is100ContinueExpected(request)) {
			ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
		}

		ByteBuf buf = request.content();
		byte[] resp = new byte[buf.readableBytes()];
		buf.readBytes(resp);

		String service_name = request.headers().get("service_name");
		String method_name = request.headers().get("method_name");

		Entry<BlockingService, ExtensionRegistry> pair = blockingServiceMap.get(service_name);
		final BlockingService blockingService = pair.getKey();
		final ExtensionRegistry registry = pair.getValue();
		final MethodDescriptor methodDescriptor = blockingService.getDescriptorForType().findMethodByName(method_name);
		Message methodRequest = blockingService.getRequestPrototype(methodDescriptor).newBuilderForType().mergeFrom(resp, registry).build();
		Message callBlockingMethod = blockingService.callBlockingMethod(methodDescriptor, new InfoGen_RPCController(), methodRequest);

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(callBlockingMethod.toByteArray()));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		response.headers().setInt(InfoGen_Header.x_sequence.key, response.headers().getInt(InfoGen_Header.x_sequence.key, 0));

		String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
			if (!cookies.isEmpty()) {
				for (Cookie cookie : cookies) {
					response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
				}
			}
		}

		boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);
		if (keepAlive) {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			ctx.write(response);
		} else {
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
			ctx.write(response);
			ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void registerService(final BlockingService service) {
		if (blockingServiceMap.containsKey(service.getDescriptorForType().getFullName())) {
			throw new IllegalArgumentException("BlockingService already registered");
		}
		final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
		service.getDescriptorForType().getFile().getExtensions().forEach(extensionRegistry::add);
		blockingServiceMap.put(service.getDescriptorForType().getFullName(), new DefaultEntry<BlockingService, ExtensionRegistry>(service, extensionRegistry));
	}

}