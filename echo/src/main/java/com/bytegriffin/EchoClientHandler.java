package com.bytegriffin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	private final ByteBuf firstMessage;

	EchoClientHandler() {
		firstMessage = Unpooled.buffer(EchoClient.SIZE);
		for (int i = 0; i < firstMessage.capacity(); i ++) {
			firstMessage.writeByte((byte) i);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		//ctx.writeAndFlush(firstMessage);
		ctx.writeAndFlush(Unpooled.copiedBuffer("This msg from clien!", CharsetUtil.UTF_8));
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;
		System.out.println("Msg Form Server: " + in.toString(CharsetUtil.UTF_8));
		//ctx.write(msg);//放开这个注释，客户端将再次将服务器返回的数据发送到服务器端，一直来回互相发送
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
