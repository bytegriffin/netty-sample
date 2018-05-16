package com.bytegriffin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DiscardClientHandler extends SimpleChannelInboundHandler<Object> {

	private ByteBuf content;
	private ChannelHandlerContext ctx;

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		this.ctx = ctx;
		content = ctx.alloc().directBuffer(DiscardClient.SIZE).writeZero(DiscardClient.SIZE);
		generateTraffic();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		content.release();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Client 收到:" + msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	long counter;

	private void generateTraffic() {
		ctx.writeAndFlush(content.retainedDuplicate()).addListener(trafficGenerator);
	}

	private final ChannelFutureListener trafficGenerator = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) {
			if (future.isSuccess()) {
				generateTraffic();
			} else {
				future.cause().printStackTrace();
				future.channel().close();
			}
		}
	};
}
