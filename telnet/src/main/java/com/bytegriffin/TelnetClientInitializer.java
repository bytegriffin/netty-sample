package com.bytegriffin;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * 每个新的channel对应一个新的配置
 * @author bytegriffin
 *
 */
public class TelnetClientInitializer extends ChannelInitializer<SocketChannel> {

	private static final TelnetClientHandler CLIENT_HANDLER = new TelnetClientHandler();
	
	private final SslContext sslCtx;
	public TelnetClientInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc(), TelnetClient.HOST, TelnetClient.PORT));
		}
		pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast(new StringDecoder());
		pipeline.addLast(new StringEncoder());
		//这里可加入其他业务逻辑
		pipeline.addLast(CLIENT_HANDLER);
	}

}
