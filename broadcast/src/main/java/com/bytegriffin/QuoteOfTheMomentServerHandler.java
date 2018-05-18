package com.bytegriffin;

import java.util.Random;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class QuoteOfTheMomentServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

	private static final Random random = new Random();
	
	//每次收到的字符串不一致
	private static final String[] quotes = {
			"aaaaaaaaaa",
			"bbbbbbbbbb",
			"cccccccccc",
			"dddddddddd"
	};
	
	private static String nextQuote() {
		int quoteId;
		synchronized (random) {
			quoteId = random.nextInt(quotes.length);
		}
		return quotes[quoteId];
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		System.err.println(msg);
		if ("QOTM?".equals(msg.content().toString(CharsetUtil.UTF_8))) {
			ctx.write(new DatagramPacket(Unpooled.copiedBuffer("QOTM: " + nextQuote(), CharsetUtil.UTF_8), msg.sender()));
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
	}
}
