package com.infogen.rpc.tracking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.rpc.filter.InfoGen_Filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class InfoGen_Server_Filter extends InfoGen_Filter {

	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Server_Filter.class.getName());
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