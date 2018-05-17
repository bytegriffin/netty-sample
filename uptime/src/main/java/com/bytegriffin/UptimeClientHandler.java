package com.bytegriffin;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @Sharable，全局只有一个handler实例，它会被多个Channel的Pipeline共享，会被多线程并发调用，因此它不是线程安全的
 * @author bytegriffin
 *
 */
@Sharable
public class UptimeClientHandler extends SimpleChannelInboundHandler<Object>{

	long startTime = -1;

	/**
	 * 建立连接
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		 if (startTime < 0) {
			 startTime = System.currentTimeMillis();
		 }
		 println("1.Connected to: " + ctx.channel().remoteAddress());
	}
	
	/**
	 * 读取数据：丢弃数据
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// Discard received data
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		if (!(evt instanceof IdleStateEvent)) {
			 return;
		}
		IdleStateEvent e = (IdleStateEvent) evt;
		if (e.state() == IdleState.READER_IDLE) {
			println("2.Disconnecting due to no inbound traffic");
			ctx.close();//关闭连接
		}
	}

	/**
	 * 关闭连接之后，会自动进入inactive状态
	 */
	@Override
	public void channelInactive(final ChannelHandlerContext ctx) {
		println("3.Disconnected from: " + ctx.channel().remoteAddress());
	}

	/**
	 * inactive之后会进入unregister状态，重连
	 */
	@Override
	public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
		println("4.Sleeping for: " + UptimeClient.RECONNECT_DELAY + 's');
		ctx.channel().eventLoop().schedule(new Runnable() {
			@Override
			public void run() {
				println("5.Reconnecting to: " + UptimeClient.HOST + ':' + UptimeClient.PORT);
				UptimeClient.connect();
			}
		}, UptimeClient.RECONNECT_DELAY, TimeUnit.SECONDS);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	void println(String msg) {
		if (startTime < 0) {
			System.err.format("[SERVER IS DOWN] %s%n", msg);
		} else {
			System.err.format("[UPTIME: %5ds] %s%n", (System.currentTimeMillis() - startTime) / 1000, msg);
		}
	}

}
