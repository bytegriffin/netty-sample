package com.bytegriffin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端请求处理
 * @author bytegriffin
 *
 */
public class SimpleClientHandler extends ChannelInboundHandlerAdapter{
	
	/**
	 * 读取服务端消息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf result = (ByteBuf)msg;
		byte[] array = new byte[result.readableBytes()];
		result.readBytes(array);
		System.out.println("服务端发来的消息："+new String(array));
		result.release();
	}
	
	/**
	 * 当出现异常时关闭连接
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
	/**
	 * 连接成功后，向server发送消息
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception{
		String msg = "你好，服务端，我是客户端。";
		ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
		encoded.writeBytes(msg.getBytes());
		ctx.write(encoded);
		ctx.flush();
	}

}
