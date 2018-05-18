package com.bytegriffin;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.ssl.SslContext;

public class FactorialClientInitializer extends ChannelInitializer<SocketChannel>  {

	private final SslContext sslCtx;
	
	public FactorialClientInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc(), FactorialClient.HOST, FactorialClient.PORT));
		}
		
		//压缩数据流
		pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
		pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
		//解码、编码
		pipeline.addLast(new BigIntegerDecoder());
		pipeline.addLast(new NumberEncoder());
		
		pipeline.addLast(new FactorialClientHandler());
	}

}
