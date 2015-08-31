package com.infogen.rpc;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

public class InfoGen_server_filter extends SimpleChannelInboundHandler<FullHttpRequest> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		System.out.println("filter");
		ctx.fireChannelRead(request);
	}

}