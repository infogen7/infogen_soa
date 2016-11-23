package com.infogen.rpc;

import java.util.Map;

import com.infogen.InfoGen;
import com.infogen.rpc.filter.InfoGen_Filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class InfoGen_RPC_Filter extends InfoGen_Filter {

	@Override
	public Boolean doFilter(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.filter.InfoGen_Filter#init(java.util.Map)
	 */
	@Override
	public void init(Map<String, String> filterConfig) {
		// TODO Auto-generated method stub
		InfoGen.aop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.rpc.filter.InfoGen_Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}