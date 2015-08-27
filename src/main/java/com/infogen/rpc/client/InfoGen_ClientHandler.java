package com.infogen.rpc.client;

import com.google.protobuf.RpcCallback;
import com.infogen.core.util.map.LRULinkedHashMap;
import com.infogen.util.InfoGen_Header;

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

public class InfoGen_ClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

	private LRULinkedHashMap<Long, RpcCallback<Object>> map;

	public InfoGen_ClientHandler(LRULinkedHashMap<Long, RpcCallback<Object>> map) {
		this.map = map;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
		HttpHeaders headers = response.headers();
		String sequence = headers.get(InfoGen_Header.x_sequence.key);
		if (sequence != null) {
			RpcCallback<Object> callback = map.get(Long.valueOf(sequence));
			if (callback != null) {
				ByteBuf buf = response.content();
				byte[] resp = new byte[buf.readableBytes()];
				buf.readBytes(resp);
				callback.run(new InfogenFullHttpResponse(headers, resp));
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}