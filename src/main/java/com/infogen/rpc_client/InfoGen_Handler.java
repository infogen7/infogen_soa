package com.infogen.rpc_client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.infogen.rpc.header.X_HttpHeaderNames;
import com.infogen.structure.map.LRULinkedHashMap;

import io.netty.buffer.ByteBuf;

/**
* @author larry/larrylv@outlook.com/创建时间 2015年8月25日 下午6:06:27
* @since 1.0
* @version 1.0
*/

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class InfoGen_Handler extends SimpleChannelInboundHandler<FullHttpResponse> {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Handler.class.getName());
	private LRULinkedHashMap<Long, SimpleStatus> map;

	public InfoGen_Handler(LRULinkedHashMap<Long, SimpleStatus> map) {
		this.map = map;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
		HttpHeaders headers = response.headers();
		String sequence = headers.get(X_HttpHeaderNames.x_sequence.key);
		HttpResponseStatus status = response.status();
		int code = status.code();
		if (code == 100) {
			return;
		}

		if (sequence != null) {
			SimpleStatus simplestatus = map.get(Long.valueOf(sequence));
			if (simplestatus != null) {
				ByteBuf buf = response.content();
				byte[] resp = new byte[buf.readableBytes()];
				buf.readBytes(resp);
				try {
					simplestatus.callback.run(simplestatus.responsePrototype.getParserForType().parseFrom(resp));
				} catch (InvalidProtocolBufferException e) {
					LOGGER.error(e.getMessage(), e);
					simplestatus.controller.setFailed(e.getMessage());
				}
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}