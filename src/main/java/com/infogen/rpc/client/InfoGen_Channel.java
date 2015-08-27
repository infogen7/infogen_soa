package com.infogen.rpc.client;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.infogen.core.util.map.LRULinkedHashMap;
import com.infogen.rpc.callback.InfoGen_RPCCallback;
import com.infogen.util.InfoGen_Header;

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
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

/**
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年8月26日 下午4:36:20
 * @since 1.0
 * @version 1.0
 */
public class InfoGen_Channel implements BlockingRpcChannel {
	public static Integer socket_timeout = 30_000;// 数据传输时间
	public static Integer connect_timeout = 3_000;// 连接时间

	private final LRULinkedHashMap<Long, RpcCallback<Object>> map = new LRULinkedHashMap<>(500000);
	private final AtomicLong atomic_long = new AtomicLong(0);
	private String host = "127.0.0.1";
	private Integer port = 8080;
	private final AtomicReference<Channel> atomic_channel = new AtomicReference<>();
	private final AtomicReference<EventLoopGroup> atomic_group = new AtomicReference<>();

	public InfoGen_Channel(String host, Integer port) throws InterruptedException {
		super();
		this.host = host;
		this.port = port;
	}

	public InfoGen_Channel connect() throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		atomic_group.set(group);

		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new HttpClientCodec());
				p.addLast(new HttpContentDecompressor());
				p.addLast(new HttpObjectAggregator(1048576));
				p.addLast(new InfoGen_ClientHandler(map));
			}
		});
		atomic_channel.set(bootstrap.connect(host, port).sync().channel());
		return this;
	}

	public InfoGen_Channel wait_close() throws InterruptedException {
		atomic_channel.get().closeFuture().sync();
		return this;
	}

	public InfoGen_Channel shutdown() {
		atomic_group.get().shutdownGracefully();
		return this;
	}

	// public Channel getChannel() throws UnexpectedServiceException {
	// if (!this.channel.get().isConnected()) {
	// // FIXME: is awaitUninterruptibly is really necessary here?
	// final ChannelFuture connectFuture = bootstrap.connect().awaitUninterruptibly();
	// final Throwable cause = connectFuture.getCause();
	// if (cause != null) {
	// throw new UnexpectedServiceException(cause);
	// }
	// this.channel.set(connectFuture.getChannel());
	// }
	// return this.channel.get();
	// }

	@Override
	public Message callBlockingMethod(MethodDescriptor method, RpcController controller, Message request, Message responsePrototype) throws ServiceException {
		if (controller == null) {
			controller = new InfoGen_RPCController();
		}

		InfoGen_RPCCallback callback = new InfoGen_RPCCallback();
		controller.notifyOnCancel(callback);

		long sequence = atomic_long.getAndIncrement();
		map.put(sequence, callback);

		DefaultFullHttpRequest httprequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/", Unpooled.copiedBuffer(request.toByteArray()));
		httprequest.headers().set(HttpHeaderNames.HOST, host);
		httprequest.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
		httprequest.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httprequest.content().readableBytes());
		httprequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		httprequest.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		httprequest.headers().set(InfoGen_Header.x_sequence.key, sequence);
		httprequest.headers().set("service_name", method.getService().getFullName());
		httprequest.headers().set("method_name", method.getName());

		httprequest.headers().set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(new DefaultCookie("my-cookie", "foo"), new DefaultCookie("another-cookie", "bar")));

		atomic_channel.get().writeAndFlush(httprequest);

		Object object = callback.get(socket_timeout);
		if (object == null) {
			throw new ServiceException("timeout");
		}

		InfogenFullHttpResponse response = (InfogenFullHttpResponse) object;
		try {
			return responsePrototype.getParserForType().parseFrom(response.getResp());
		} catch (InvalidProtocolBufferException e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
}
