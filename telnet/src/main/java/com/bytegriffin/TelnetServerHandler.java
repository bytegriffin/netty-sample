package com.bytegriffin;

import java.net.InetAddress;
import java.util.Date;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		 ctx.write("欢迎 " + InetAddress.getLocalHost().getHostName() + "!\r\n");
		 ctx.write("现在时间是 " + new Date() + ".\r\n");
		 ctx.flush();
	}

	/**
	 * 回写并开启客户端关闭监听
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		String response;
		boolean close = false;
		if (msg.isEmpty()) {
			response = "请输入：\r\n";
		}else if ("exit".equals(msg.toLowerCase())) {
			response = "再见!\r\n";
			close = true;
		} else {
			response = "你是说 '" + msg + "' 么?\r\n";
		}
		//将客户端发送的信息进行回写
		ChannelFuture future = ctx.write(response);
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);//启动关闭监听
		}
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
