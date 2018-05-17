package com.bytegriffin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 连接成功后超过10秒没有io读操作，那么连接就会 失效
 * 之后每隔5秒会进行一次重连，连接成功后再循环
 * @author bytegriffin
 *
 */
public class UptimeClient {
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
	//每隔5秒进行重连
	static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "5"));
	//当服务器没反应时10秒后就进行重连
	private static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

	private static final UptimeClientHandler handler = new UptimeClientHandler();

	private static final Bootstrap bs = new Bootstrap();

	//周期性地打印时间
	static void connect() {
	  bs.connect().addListener(new ChannelFutureListener() {
		  @Override
		  public void operationComplete(ChannelFuture future) throws Exception {
			  if (future.cause() != null) {
				  handler.startTime = -1;
				  handler.println("Failed to connect: " + future.cause());
			  }
				
	      }
	  });
	}

	public static void main(String[] args) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		bs.group(group).channel(NioSocketChannel.class)
			.remoteAddress(HOST, PORT)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0), handler);//超过10秒没读取数据，就会关闭连接
				}
			 });
		bs.connect();
	}
	
}
