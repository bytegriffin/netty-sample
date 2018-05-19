package com.bytegriffin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * socket代理服务器 <br>
 * 注意不是http代理服务器，否则会报错<br>
 * 测试时要结合echo项目，并且确保EchoClient类中的HOST和PORT接口
 * 与HexDumpProxy类中的REMOTE_HOST和REMOTE_PORT一致<br>
 * 让EchoClient通过代理服务器HexDumpProxy去访问EchoServer，代理服务器也会收到EchoClient发送的消息<br>
 * 启动顺序：EchoServer->HexDumpProxy->EchoClient
 * @author bytegriffin
 *
 */
public class HexDumpProxy {

	static final int LOCAL_PORT = Integer.parseInt(System.getProperty("localPort", "8787"));

	static final String REMOTE_HOST = System.getProperty("remoteHost", "localhost");

	static final int REMOTE_PORT = Integer.parseInt(System.getProperty("remotePort", "8007"));

	public static void main(String[] args) throws Exception {
		System.err.println("Proxying *:" + LOCAL_PORT + " to " + REMOTE_HOST + ':' + REMOTE_PORT + " ...");

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new HexDumpProxyInitializer(REMOTE_HOST, REMOTE_PORT))
					.childOption(ChannelOption.AUTO_READ, false).bind(LOCAL_PORT).sync().channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
