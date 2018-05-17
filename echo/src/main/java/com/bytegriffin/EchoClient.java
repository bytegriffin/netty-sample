package com.bytegriffin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class EchoClient {
	private static final boolean SSL = System.getProperty("ssl") != null;
	private static final String HOST = System.getProperty("host", "127.0.0.1");
	private static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
	static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

	public static void main(String[] args) throws Exception {
		final SslContext sslCtx;
		if (SSL) {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			sslCtx = null;
		}
		EventLoopGroup group = new NioEventLoopGroup();
		try{
			Bootstrap b = new Bootstrap();
			b.group(group)
					.channel(NioSocketChannel.class)
					//启用Nagle算法，表示一有数据就立刻发送
					.option(ChannelOption.TCP_NODELAY,true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) {
							ChannelPipeline p = ch.pipeline();
							if (sslCtx != null) {
								p.addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT));
							}
							p.addLast(new EchoClientHandler());
						}
					});
			ChannelFuture f = b.connect(HOST, PORT).sync();
			f.channel().closeFuture().sync();
		}finally{
			group.shutdownGracefully();
		}

	}

}
