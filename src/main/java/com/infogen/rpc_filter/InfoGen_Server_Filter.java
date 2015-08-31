package com.infogen.rpc_filter;

import org.apache.log4j.Logger;

import com.infogen.rpc.filter.InfoGen_Filter;
import com.infogen.tracking.InfoGen_RPC_Tracking_Handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class InfoGen_Server_Filter extends InfoGen_Filter {

	private static final Logger LOGGER = Logger.getLogger(InfoGen_Server_Filter.class.getName());
	private final InfoGen_RPC_Tracking_Handle track = new InfoGen_RPC_Tracking_Handle();

	@Override
	public Boolean doFilter(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		try {
			track.doFilter(ctx, request, response);
		} catch (Exception e) {
			LOGGER.error("调用链日志初始化失败", e);
		}
		return true;
	}

}