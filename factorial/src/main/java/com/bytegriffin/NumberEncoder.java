package com.bytegriffin;

import java.math.BigInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义编码器
 * 将42 变成 { 'F', 0, 0, 0, 1, 42 }
 * @author bytegriffin
 *
 */
public class NumberEncoder extends MessageToByteEncoder<Number>{

	@Override
	protected void encode(ChannelHandlerContext ctx, Number msg, ByteBuf out) {
		BigInteger v;
		if (msg instanceof BigInteger) {
			v = (BigInteger) msg;
		} else {
			v = new BigInteger(String.valueOf(msg));
		}
		// Convert the number into a byte array.
		byte[] data = v.toByteArray();
		int dataLength = data.length;
		// Write a message.
		out.writeByte((byte) 'F'); // magic number
		out.writeInt(dataLength);  // data length
		out.writeBytes(data);      // data
	}

}
