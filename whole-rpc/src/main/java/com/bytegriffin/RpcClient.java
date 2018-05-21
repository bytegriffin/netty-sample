package com.bytegriffin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 客户端<br>
 * 利用包头中的消息长度来处理半包和粘包<br>
 * 自动重连
 * 
 * @author bytegriffin
 *
 */
public final class RpcClient {

	private String host;
	private int port;
	private static Bootstrap bootstrap;
	private static Channel channel;
	private static EventLoopGroup worker;
	private static AtomicInteger retryCount;

	private RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	static void conn(String host, int port) {
		RpcClient client = new RpcClient(host, port);
		client.init();
	}

	void init() {
		bootstrap = new Bootstrap();
		worker = new NioEventLoopGroup();
		retryCount = new AtomicInteger();
		bootstrap.group(worker);
		bootstrap.channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.TRACE));
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline()
						.addLast(new ProtobufVarint32FrameDecoder())//解码1.根据byte的头长度来分割  
						.addLast(new ProtobufDecoder(Message.Request.getDefaultInstance()))//解码2.byte转化为实体类Message.Request
						.addLast(new ProtobufVarint32LengthFieldPrepender())//编码3.byte数组头加上，实体类的长度  
						.addLast(new ProtobufEncoder())//编码2.Message.Request实体转化为byte数组 
						.addLast(new RpcClientHandler());//编码1. 写入Message.Request实体实
			}
		});
		bootstrap.remoteAddress(host, port);
		retryConnection();
	}

	static void close() {
		worker.shutdownGracefully();
	}

	static void retryConnection() {
		if (channel != null && channel.isActive()) {
			return;
		}
		ChannelFuture future = bootstrap.connect().awaitUninterruptibly();
		channel = future.channel();
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture futureListener) throws Exception {
				if (futureListener.isSuccess()) {
					if(retryCount.get() == 0){
						retryCount.incrementAndGet();
						System.out.println("客户端连接服务器成功。");
					} else {
						retryCount.set(1);
						System.out.println("客户端重新连接服务器成功。");
					}
				} else {
					if(retryCount.get() > 3){
						System.out.println("客户端连接服务器失败，并且超出最大连接次数，准备关闭。。。");
						close();
					} else {
						System.out.println("客户端连接服务器失败，1秒后将自动重连，重连次数为："+retryCount.get());
						futureListener.channel().eventLoop().schedule(new Runnable() {
							@Override
							public void run() {
								retryCount.incrementAndGet();
								retryConnection();
							}
						}, 1, TimeUnit.SECONDS);
					}
					
				}
			}
		});
	}

	static void send() {
		if(channel != null){
			Message.Request.Builder request = Message.Request.newBuilder();
			request.setRequestId("requestId");
			channel.writeAndFlush(request);
		}
	}

	public static void main(String[] args) throws Exception {
		RpcClient.conn(Session.server_ip, Session.server_port);
		for (int i = 0; i < 3; i++) {
			send();
		}
	}

}