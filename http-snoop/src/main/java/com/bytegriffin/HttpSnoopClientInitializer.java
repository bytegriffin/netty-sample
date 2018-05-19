package com.bytegriffin;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.ssl.SslContext;

public class HttpSnoopClientInitializer extends ChannelInitializer<SocketChannel>{

	private final SslContext sslCtx;

	public HttpSnoopClientInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		// Enable HTTPS if necessary.
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		//压缩/解压缩
		p.addLast(new HttpClientCodec());
		// 解压缩
		p.addLast(new HttpContentDecompressor());
		// 如果不想处理httpContent可以注销下面一行
		// p.addLast(new HttpObjectAggregator(1048576));
		p.addLast(new HttpSnoopClientHandler());
	}

}
