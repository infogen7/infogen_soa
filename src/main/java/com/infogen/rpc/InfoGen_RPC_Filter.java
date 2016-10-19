package com.infogen.tracking;

import com.infogen.rpc.filter.InfoGen_Filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class InfoGen_RPC_Track_Filter extends InfoGen_Filter {

	@Override
	public Boolean doFilter(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		return true;
	}

}