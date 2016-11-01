package com.infogen.rpc.server;

import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.BlockingService;
import com.infogen.rpc.filter.InfoGen_Filter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年8月26日 上午10:22:35
 * @since 1.0
 * @version 1.0
 */
public class Server {
	private Integer port = 8080;
	private AtomicReference<EventLoopGroup> atomic_boss_group = new AtomicReference<>();
	private AtomicReference<EventLoopGroup> atomic_worker_group = new AtomicReference<>();

	public Server(Integer port) {
		this.port = port;
	}

	public void serve() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		ServerBootstrap server_bootstrap = new ServerBootstrap();
		server_bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		server_bootstrap.group(bossGroup, workerGroup);
		server_bootstrap.channel(NioServerSocketChannel.class);
		server_bootstrap.handler(new LoggingHandler(LogLevel.INFO));

		server_bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) {
				ChannelPipeline p = ch.pipeline();

				// p.addLast(new HttpServerCodec());
				// InboundHandler
				p.addLast(new HttpRequestDecoder());
				p.addLast(new HttpObjectAggregator(1048576));
				// OutboundHandler
				p.addLast(new HttpContentCompressor());
				p.addLast(new HttpResponseEncoder());

				// Filter
				// 如果不是 @Sharable 的 所以必须每次 new 一个新的
				p.addLast(new ServerHandler());
			}
		});

		atomic_boss_group.set(bossGroup);
		atomic_worker_group.set(workerGroup);
		server_bootstrap.bind(port).sync().channel();
	}

	public Server shutdown() throws InterruptedException {
		atomic_boss_group.get().shutdownGracefully();
		atomic_worker_group.get().shutdownGracefully();
		return this;
	}

	public Server add_filter(final InfoGen_Filter filter) {
		ServerHandler.add_filter(filter);
		return this;
	}

	public Server registerService(final BlockingService service) {
		ServerHandler.registerService(service);
		return this;
	}

}