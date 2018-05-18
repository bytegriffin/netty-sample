package com.bytegriffin;

import java.math.BigInteger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 处理有状态信息
 * @author bytegriffin
 *
 */
public class FactorialServerHandler  extends SimpleChannelInboundHandler<BigInteger>  {

	private BigInteger lastMultiplier = new BigInteger("1");
	private BigInteger factorial = new BigInteger("1");

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BigInteger msg) throws Exception {
		lastMultiplier = msg;
		factorial = factorial.multiply(msg);
		ctx.writeAndFlush(factorial);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.err.printf("Factorial of %,d is: %,d%n", lastMultiplier, factorial);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

}
