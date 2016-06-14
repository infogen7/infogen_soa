package com.infogen.tracking;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.infogen.rpc.filter.InfoGen_Filter;
import com.infogen.rpc.tools.Tool_RPC;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AsciiString;

public class InfoGen_RPC_Track_Filter extends InfoGen_Filter {

	private static final Logger LOGGER = LogManager.getLogger(InfoGen_RPC_Track_Filter.class.getName());

	@Override
	public Boolean doFilter(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		try {
			CallChain callchain = new CallChain();
			HttpHeaders headers = request.headers();
			// traceid
			String traceid = headers.get(new AsciiString(HTTP_Header.x_track_id.key));
			if (traceid == null || traceid.isEmpty()) {
				traceid = UUID.randomUUID().toString().replaceAll("-", "");
			}
			callchain.setTrackid(traceid);
			// identify
			callchain.setIdentify(headers.get(new AsciiString(HTTP_Header.x_identify.key)));
			// session id
			callchain.setSessionid(headers.get(new AsciiString(HTTP_Header.x_session_id.key)));
			// sequence
			String x_sequence = headers.get(new AsciiString(HTTP_Header.x_sequence.key));
			Integer sequence = x_sequence == null ? 0 : Integer.valueOf(x_sequence);
			callchain.setSequence(sequence + 1);
			// referer ip
			callchain.setReferer_ip(Tool_RPC.get_ip(ctx, request));
			// target
			callchain.setTarget(request.uri());

			// Referer
			String x_referer = headers.get(new AsciiString(HTTP_Header.x_referer.key));
			x_referer = x_referer == null ? headers.get(new AsciiString(HTTP_Header.Referer.key)) : x_referer;
			callchain.setReferer(x_referer);
			//
			ThreadLocal_Tracking.setCallchain(callchain);
		} catch (Exception e) {
			LOGGER.error("调用链日志初始化失败", e);
		}
		return true;
	}

}