package com.infogen.tracking.rpc_filter;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.infogen.rpc.tools.Tool_RPC;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.ThreadLocal_Tracking;
import com.infogen.util.HTTP_Header;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.AsciiString;

/**
 * HTTP协议下记录调用链的处理类
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年5月4日 下午2:11:06
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_RPC_Tracking_Handle {

	public static String sessionid_name = "token";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.infogen.web.InfoGen_SOA_Filter_Handle#doFilter(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public CallChain doFilter(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) throws IOException, ServletException {
		CallChain callchain = new CallChain();
		HttpHeaders headers = request.headers();
		// traceid
		String traceid = headers.get(new AsciiString(HTTP_Header.x_track_id.key));
		if (traceid == null || traceid.isEmpty()) {
			callchain.setTrackid(UUID.randomUUID().toString().replaceAll("-", ""));
			// identify
			String identify = Tool_RPC.get_cookie(request, HTTP_Header.x_identify.key);
			if (identify == null) {
				identify = UUID.randomUUID().toString().replaceAll("-", "");
				Tool_RPC.set_cookie(response, HTTP_Header.x_identify.key, identify);
			}
			callchain.setIdentify(identify);
			// sequence
			callchain.setSequence(0);
			// 注意:可能为空
			// Referer
			callchain.setReferer(headers.get(new AsciiString(HTTP_Header.Referer.key)));
			// session id
			String session_id = Tool_RPC.get_cookie(request, sessionid_name);
			if (session_id == null) {
				session_id = headers.get(new AsciiString(HTTP_Header.x_session_id.key));
			}
			callchain.setSessionid(session_id);
		} else {
			callchain.setTrackid(traceid);
			// identify
			callchain.setIdentify(headers.get(new AsciiString(HTTP_Header.x_identify.key)));
			// sequence
			String x_sequence = headers.get(new AsciiString(HTTP_Header.x_sequence.key));
			Integer sequence = x_sequence == null ? 0 : Integer.valueOf(x_sequence);
			callchain.setSequence(sequence + 1);

			// Referer
			callchain.setReferer(headers.get(new AsciiString(HTTP_Header.x_referer.key)));
			// session id
			callchain.setSessionid(headers.get(new AsciiString(HTTP_Header.x_session_id.key)));
		}

		// referer ip
		callchain.setReferer_ip(Tool_RPC.get_ip(ctx, request));
		// target
		callchain.setTarget(request.uri());
		//
		ThreadLocal_Tracking.setCallchain(callchain);
		return callchain;
	}
}