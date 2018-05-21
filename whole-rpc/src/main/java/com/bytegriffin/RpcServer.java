package com.bytegriffin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 服务端<br>
 * 利用包头中的消息长度来处理半包和粘包<br>
 * 自动心跳检测
 * 
 * @author bytegriffin
 *
 */
public class RpcServer {

	static final ServerBootstrap bootstrap = new ServerBootstrap();
	private static int port;

	static RpcServer create(int port){
		RpcServer server = new RpcServer();
		server.setPort(port);
		return server;
	}

	public  void setPort(int port) {
		RpcServer.port = port;
	}

	public void run() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 128) 
				.childOption(ChannelOption.SO_KEEPALIVE, true) 
				.childOption(ChannelOption.TCP_NODELAY, true)
				.handler(new LoggingHandler(LogLevel.TRACE))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new IdleStateHandler(Session.READ_IDLE_TIME_OUT, Session.WRITE_IDLE_TIME_OUT, Session.IDLE_TIME_OUT))
								.addLast(new ProtobufVarint32FrameDecoder()) //解码1.根据byte的头长度来分割  
								.addLast(new ProtobufDecoder(Message.Response.getDefaultInstance()))//解码2.byte转化为实体类Message.Response
								.addLast(new ProtobufVarint32LengthFieldPrepender()) //编码3.byte数组头加上，实体类的长度  
								.addLast(new ProtobufEncoder()) //编码2.Message.Response实体转化为byte数组 
								.addLast(new RpcServerHandler());//编码1. 写入Message.Response实体实
					}
			});
			ChannelFuture future = bootstrap.bind(port).sync();
			System.out.println("服务端启动成功。");
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			System.err.println("服务器端有异常发生："+e);
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		RpcServer.create(Session.server_port).run();
	}

}
