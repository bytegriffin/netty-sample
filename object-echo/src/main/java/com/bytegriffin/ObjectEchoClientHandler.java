package com.bytegriffin;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ObjectEchoClientHandler extends ChannelInboundHandlerAdapter {

	private final List<Integer> firstMessage;

	ObjectEchoClientHandler() {
		firstMessage = new ArrayList<Integer>(ObjectEchoClient.SIZE);
		for (int i = 0; i < ObjectEchoClient.SIZE; i++) {
			firstMessage.add(Integer.valueOf(i));
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(firstMessage);//第一次请求发送一个list对象
	}

	/**
	 * 客户端将再次将服务器返回的数据发送到服务器端，一直来回互相发送
	 * @param ctx
	 * @param msg
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		 ctx.write(msg);
		 System.err.println("服务器端返回的数据："+msg.toString());
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
