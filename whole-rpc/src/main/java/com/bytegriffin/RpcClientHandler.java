package com.bytegriffin;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<Message.Request> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message.Request msg) throws Exception {
		System.out.println("客户端["+Session.getIP(ctx)+"]接收数据["+msg.getRequestId()+"]成功。");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端["+Session.getIP(ctx)+"]连接成功。");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		RpcClient.retryConnection();
		System.out.println("客户端["+Session.getIP(ctx)+"]连接失败。");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("客户端发现异常：" + cause);
		ctx.close();
	}

}