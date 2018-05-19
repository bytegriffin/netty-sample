package com.bytegriffin;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel> {
	
	private final String remoteHost;
	
	private final int remotePort;
	
	public HexDumpProxyInitializer(String remoteHost, int remotePort) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		 ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO),
				 new HexDumpProxyFrontendHandler(remoteHost, remotePort));
	}

}
