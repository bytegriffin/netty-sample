package com.bytegriffin.msg;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.UdtMessage;
import io.netty.channel.udt.nio.NioUdtProvider;

/**
 * Handler implementation for the echo peer. It initiates the ping-pong traffic
 * between the echo peers by sending the first message to the other peer on
 * activation.
 */
@SuppressWarnings("deprecation")
public class MsgEchoPeerHandler extends SimpleChannelInboundHandler<UdtMessage> {

    private final UdtMessage message;

    public MsgEchoPeerHandler(final int messageSize) {
        super(false);
        final ByteBuf byteBuf = Unpooled.buffer(messageSize);
        for (int i = 0; i < byteBuf.capacity(); i++) {
            byteBuf.writeByte((byte) i);
        }
        message = new UdtMessage(byteBuf);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.err.println("ECHO active " + NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, UdtMessage message) {
        ctx.write(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}