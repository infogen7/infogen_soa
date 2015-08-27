package com.infogen.rpc;

import java.util.concurrent.atomic.AtomicReference;

import com.google.protobuf.BlockingService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
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
public class InfoGen_Server {
	private Integer port = 8080;
	private AtomicReference<Channel> atomic_channel = new AtomicReference<>();
	private InfoGen_ServerHandler handler = new InfoGen_ServerHandler();
	private AtomicReference<EventLoopGroup> atomic_boss_group = new AtomicReference<>();
	private AtomicReference<EventLoopGroup> atomic_worker_group = new AtomicReference<>();

	public InfoGen_Server(Integer port) {
		this.port = port;
	}

	public InfoGen_Server serve() throws InterruptedException {
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
				p.addLast(new HttpRequestDecoder());
				p.addLast(new HttpObjectAggregator(1048576));
				p.addLast(new HttpResponseEncoder());
				p.addLast(new HttpContentDecompressor());
				p.addLast(new InfoGen_ServerHandler());
			}
		});

		atomic_boss_group.set(bossGroup);
		atomic_worker_group.set(workerGroup);
		atomic_channel.set(server_bootstrap.bind(port).sync().channel());
		return this;
	}

	public InfoGen_Server shutdown() throws InterruptedException {
		atomic_channel.get().closeFuture().sync();
		atomic_boss_group.get().shutdownGracefully();
		atomic_worker_group.get().shutdownGracefully();
		return this;
	}

	public InfoGen_Server registerService(final BlockingService service) {
		handler.registerService(service);
		return this;
	}

}