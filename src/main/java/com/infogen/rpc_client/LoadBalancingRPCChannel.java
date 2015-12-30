package com.infogen.rpc_client;

import java.time.Clock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.ServiceException;
import com.infogen.Service;
import com.infogen.core.structure.map.LRULinkedHashMap;
import com.infogen.core.util.CODE;
import com.infogen.exception.Node_Unavailable_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.rpc.client.InfoGen_Channel;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.infogen.tracking.CallChain;
import com.infogen.tracking.HTTP_Header;
import com.infogen.tracking.ThreadLocal_Tracking;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.util.AsciiString;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月26日 下午4:36:20
 * @since 1.0
 * @version 1.0
 */
public class LoadBalancingRPCChannel extends InfoGen_Channel {
	private static final Logger LOGGER = LogManager.getLogger(LoadBalancingRPCChannel.class.getName());
	private final LRULinkedHashMap<String, Channel> map = new LRULinkedHashMap<>(5000);
	private Service service;

	public LoadBalancingRPCChannel(Service service) {
		this.service = service;
	}

	public InfoGen_Channel connect() {
		return this;
	}

	public Channel getChannel(RemoteNode node) throws InterruptedException {
		Channel channel = map.get(node.getName());
		if (channel == null) {
			channel = connect(node.getIp(), node.getRpc_port());
		} else if (!channel.isActive()) {// channel!=null && !channel.isActive()
			channel.close();
			channel = connect(node.getIp(), node.getRpc_port());
		}
		return channel;
	}

	public void writeAndFlush(DefaultFullHttpRequest httprequest) throws ServiceException {
		RemoteServer server = service.get_server();
		if (server == null) {
			LOGGER.error(CODE.service_notfound.note);
			throw new ServiceException(new Service_Notfound_Exception());
		}
		RemoteNode node = null;
		String seed = String.valueOf(Clock.systemDefaultZone().millis());
		CallChain callChain = ThreadLocal_Tracking.getCallchain().get();
		if (callChain != null) {
			httprequest.headers().set(new AsciiString(HTTP_Header.x_session_id.key), callChain.getSessionid());
			httprequest.headers().set(new AsciiString(HTTP_Header.x_referer.key), callChain.getReferer());
			httprequest.headers().set(new AsciiString(HTTP_Header.x_track_id.key), callChain.getTrackid());
			httprequest.headers().set(new AsciiString(HTTP_Header.x_identify.key), callChain.getIdentify());
			httprequest.headers().set(new AsciiString(HTTP_Header.x_sequence.key), callChain.getSequence());
		}

		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node(seed);
			if (node == null) {
				LOGGER.error(CODE.node_unavailable.note);
				throw new ServiceException(new Node_Unavailable_Exception());
			}
			try {
				getChannel(node).writeAndFlush(httprequest);
				return;
			} catch (InterruptedException e) {
				LOGGER.error("发送消息失败", e);
				server.disabled(node);
				continue;
			}
		}
		LOGGER.error(CODE.node_unavailable.note);
		throw new ServiceException(new Node_Unavailable_Exception());
	}
}
