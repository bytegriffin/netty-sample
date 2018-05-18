package com.bytegriffin;

import java.math.BigInteger;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

/**
 * 自定义解码器
 * 将{'F', 0, 0, 0, 1, 42}变成BigInteger("42")
 * @author bytegriffin
 *
 */
public class BigIntegerDecoder extends ByteToMessageDecoder{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 等待直到长度可用
		if (in.readableBytes() < 5) {
			return;
		}
		in.markReaderIndex();
		//检查魔法数字
		int magicNumber = in.readUnsignedByte();
		if (magicNumber != 'F') {
			in.resetReaderIndex();
			throw new CorruptedFrameException("Invalid magic number: " + magicNumber);
		}
		//等待知道数据可用
		int dataLength = in.readInt();
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		
		byte[] decoded = new byte[dataLength];
		in.readBytes(decoded);
		
		out.add(new BigInteger(decoded));
	}

}
