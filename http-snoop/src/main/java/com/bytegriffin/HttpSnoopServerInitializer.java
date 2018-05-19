package com.bytegriffin;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

public class HttpSnoopServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	
	public HttpSnoopServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		p.addLast(new HttpRequestDecoder());
		// 如果不想处理HttpChunks可以注销下面一行.
		//p.addLast(new HttpObjectAggregator(1048576));
		// http编码
		p.addLast(new HttpResponseEncoder());
		// 如果不想压缩可以注销下面一行
		//p.addLast(new HttpContentCompressor());
		p.addLast(new HttpSnoopServerHandler());
	}

}
