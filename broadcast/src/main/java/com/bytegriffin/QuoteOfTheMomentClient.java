package com.bytegriffin;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;

/**
 * 一个UDP广播客户端
 * @author bytegriffin
 *
 */
public final class QuoteOfTheMomentClient {
	
	static final int PORT = Integer.parseInt(System.getProperty("port", "7686"));
	
	public static void main(String[] args) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			 Bootstrap b = new Bootstrap();
			//设置广播，数据包
			 b.group(group)
			 	.channel(NioDatagramChannel.class)
			 	.option(ChannelOption.SO_BROADCAST, true)
			 	.handler(new QuoteOfTheMomentClientHandler());
			 //绑定0端口
			 Channel ch = b.bind(0).sync().channel();
			 //广播数据报文
			 ch.writeAndFlush(new DatagramPacket(
					 Unpooled.copiedBuffer("QOTM?", CharsetUtil.UTF_8),
					 SocketUtils.socketAddress("255.255.255.255", PORT))).sync();
			 //如果channel在5秒内没有关闭，则显示超时提示
			 if (!ch.closeFuture().await(5000)) {
				 System.err.println("QOTM request timed out.");
			 }
		} finally {
			group.shutdownGracefully();
		}
	}

}
