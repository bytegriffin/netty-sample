package com.bytegriffin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * 求5的阶乘
 * @author bytegriffin
 *
 */
public final class FactorialClient {

	static final boolean SSL = System.getProperty("ssl") != null;
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "8322"));
	static final int COUNT = Integer.parseInt(System.getProperty("count", "5"));

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
					.handler(new FactorialClientInitializer(sslCtx));
			
			ChannelFuture f = b.connect(HOST, PORT).sync();
			//调用handler
			FactorialClientHandler handler = (FactorialClientHandler) f.channel().pipeline().last();
			System.err.format("Factorial of %,d is: %,d", COUNT, handler.getFactorial());
		}finally{
			group.shutdownGracefully();
		}

	}

}
