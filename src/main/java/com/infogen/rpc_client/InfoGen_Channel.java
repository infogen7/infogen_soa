package com.infogen.rpc_client;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.infogen.exception.Timeout_Exception;
import com.infogen.rpc.InfoGen_Controller;
import com.infogen.rpc.header.X_HttpHeaderNames;
import com.infogen.rpc_client.callback.InfoGen_Callback;
import com.infogen.structure.map.LRULinkedHashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月26日 下午4:36:20
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Channel implements BlockingRpcChannel, RpcChannel {
	private static final Logger LOGGER = LogManager.getLogger(InfoGen_Channel.class.getName());
	public static Integer socket_timeout = 30_000;// 数据传输时间
	public static Integer connect_timeout = 3_000;// 连接时间

	private final LRULinkedHashMap<Long, SimpleStatus> map = new LRULinkedHashMap<>(500000);
	private final AtomicLong atomic_long = new AtomicLong(10000);

	private final AtomicReference<Channel> atomic_channel = new AtomicReference<>();
	private final EventLoopGroup group = new NioEventLoopGroup();
	private String host;
	private Integer port;

	private final transient byte[] getchannel_lock = new byte[0];

	public InfoGen_Channel(String host, Integer port) {
		this.host = host;
		this.port = port;
	}

	protected InfoGen_Channel() {
	}

	public InfoGen_Channel connect() throws InterruptedException {
		connect(host, port);
		return this;
	}

	protected Channel connect(String host, Integer port) throws InterruptedException {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) {
				ChannelPipeline p = ch.pipeline();
				// InboundHandler OutboundHandler
				p.addLast(new HttpClientCodec());

				// InboundHandler
				p.addLast(new HttpContentDecompressor());
				p.addLast(new HttpObjectAggregator(1048576));

				//
				p.addLast(new InfoGen_Handler(map));
			}
		});
		Channel channel = bootstrap.connect(host, port).sync().channel();

		atomic_channel.set(channel);
		return channel;
	}

	public InfoGen_Channel shutdown() {
		group.shutdownGracefully();
		return this;
	}

	public Channel getChannel() throws InterruptedException {
		Channel channel = atomic_channel.get();
		synchronized (getchannel_lock) {
			if (!channel.isActive()) {
				channel.close();
				connect(host, port);
			}
		}
		return channel;
	}

	public void writeAndFlush(DefaultFullHttpRequest httprequest) throws ServiceException {
		try {
			httprequest.headers().set(HttpHeaderNames.HOST, host);
			getChannel().writeAndFlush(httprequest);
		} catch (InterruptedException e) {
			LOGGER.error("发送消息失败", e);
			throw new ServiceException(e.getMessage(), e);
		}
	}

	@Override
	public Message callBlockingMethod(MethodDescriptor method, RpcController controller, Message request, Message responsePrototype) throws ServiceException {
		InfoGen_Callback<Message> callback = new InfoGen_Callback<>();
		if (controller == null) {
			controller = new InfoGen_Controller();
		}

		long sequence = atomic_long.getAndIncrement();
		SimpleStatus status = new SimpleStatus(controller, responsePrototype, callback);
		map.put(sequence, status);

		DefaultFullHttpRequest httprequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, new StringBuilder("/").append(method.getService().getFullName()).append("/").append(method.getName()).toString(), Unpooled.copiedBuffer(request.toByteArray()));
		httprequest.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		httprequest.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httprequest.content().readableBytes());
		httprequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		httprequest.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		// 异步返回必须知道该序列号才能返回调用程序结果
		httprequest.headers().set(X_HttpHeaderNames.x_sequence.key, sequence);

		writeAndFlush(httprequest);

		Message message = callback.get(socket_timeout);
		if (message == null) {
			throw new ServiceException("timeout", new Timeout_Exception());
		}
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.protobuf.RpcChannel#callMethod(com.google.protobuf.Descriptors.MethodDescriptor, com.google.protobuf.RpcController, com.google.protobuf.Message, com.google.protobuf.Message, com.google.protobuf.RpcCallback)
	 */
	@Override
	public void callMethod(MethodDescriptor method, RpcController controller, Message request, Message responsePrototype, RpcCallback<Message> callback) {
		if (controller == null) {
			controller = new InfoGen_Controller();
		}

		long sequence = atomic_long.getAndIncrement();
		SimpleStatus status = new SimpleStatus(controller, responsePrototype, callback);
		map.put(sequence, status);

		DefaultFullHttpRequest httprequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, new StringBuilder(method.getService().getFullName()).append("/").append(method.getName()).toString(), Unpooled.copiedBuffer(request.toByteArray()));
		httprequest.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		httprequest.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httprequest.content().readableBytes());
		httprequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		httprequest.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		httprequest.headers().set(X_HttpHeaderNames.x_sequence.key, sequence);

		try {
			writeAndFlush(httprequest);
		} catch (ServiceException e) {
			controller.setFailed(e.getMessage());
		}
	}

}
