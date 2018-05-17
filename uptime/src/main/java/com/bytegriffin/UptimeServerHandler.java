package com.bytegriffin;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Sharable，全局只有一个handler实例，它会被多个Channel的Pipeline共享，会被多线程并发调用，因此它不是线程安全的
 * @author bytegriffin
 *
 */
@Sharable
public class UptimeServerHandler extends SimpleChannelInboundHandler<Object> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// discard
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
