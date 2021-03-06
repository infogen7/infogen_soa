package com.infogen.rpc_client;

import java.time.Clock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.ServiceException;
import com.infogen.InfoGen_CODE;
import com.infogen.Service;
import com.infogen.exception.Node_Unavailable_Exception;
import com.infogen.exception.Service_Notfound_Exception;
import com.infogen.server.model.RemoteNode;
import com.infogen.server.model.RemoteServer;
import com.infogen.structure.map.LRULinkedHashMap;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;

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
			LOGGER.error(InfoGen_CODE.notfound_service.message);
			throw new ServiceException(new Service_Notfound_Exception());
		}
		RemoteNode node = null;
		String seed = String.valueOf(Clock.systemDefaultZone().millis());

		// 调用出错重试3次
		for (int i = 0; i < 3; i++) {
			node = server.random_node(seed);
			if (node == null) {
				LOGGER.error(InfoGen_CODE.notfound_node.message);
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
		LOGGER.error(InfoGen_CODE.notfound_node.message);
		throw new ServiceException(new Node_Unavailable_Exception());
	}
}
