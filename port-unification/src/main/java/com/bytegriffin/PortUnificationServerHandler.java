package com.bytegriffin;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

public class PortUnificationServerHandler extends ByteToMessageDecoder {

	private final SslContext sslCtx;
	private final boolean detectSsl;
	private final boolean detectGzip;

	public PortUnificationServerHandler(SslContext sslCtx) {
		this(sslCtx, true, true);
	}

	private PortUnificationServerHandler(SslContext sslCtx, boolean detectSsl, boolean detectGzip) {
		this.sslCtx = sslCtx;
		this.detectSsl = detectSsl;
		this.detectGzip = detectGzip;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 判断不同的协议
		if (isSsl(in)) {
			enableSsl(ctx);
		} else {
			// 魔法字节用来判断gzip
			final int magic1 = in.getUnsignedByte(in.readerIndex());
			final int magic2 = in.getUnsignedByte(in.readerIndex() + 1);
			// 字符串用来判断其他协议
			byte[] req = new byte[in.readableBytes()];
			in.readBytes(req);
			String body = new String(req, "UTF-8");
			if (isGzip(magic1, magic2)) {
				enableGzip(ctx);
			} else if (isHttp(body)) {
				System.out.println("转换为http协议，客户端请求内容是："+body);
				switchToHttp(ctx);
			} else {
				System.err.println("客户端发送的是未知的协议，连接已中断！");
				in.clear();
				ctx.close();
			}
		}
	}

	private boolean isSsl(ByteBuf buf) {
		if (detectSsl) {
			return SslHandler.isEncrypted(buf);
		}
		return false;
	}

	private boolean isGzip(int magic1, int magic2) {
		if (detectGzip) {
			return magic1 == 31 && magic2 == 139;
		}
		return false;
	}

	/**
	 * 假设请求的字符串中包含get或post字符串，那么就认为是http协议
	 * 
	 * @param req
	 * @return
	 */
	private boolean isHttp(String req) {
		return req.contains("get") || req.contains("post");
	}

	private void enableSsl(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.pipeline();
		p.addLast("ssl", sslCtx.newHandler(ctx.alloc()));
		p.addLast("unificationA", new PortUnificationServerHandler(sslCtx, false, detectGzip));
		p.remove(this);
	}

	private void enableGzip(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.pipeline();
		p.addLast("gzipdeflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
		p.addLast("gzipinflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
		p.addLast("unificationB", new PortUnificationServerHandler(sslCtx, detectSsl, false));
		p.remove(this);
	}

	private void switchToHttp(ChannelHandlerContext ctx) {
		ChannelPipeline p = ctx.pipeline();
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new HttpResponseEncoder());
		p.addLast("deflater", new HttpContentCompressor());
		//p.addLast("handler", new HttpSnoopServerHandler()); //自定义的http的Server Handler
		p.remove(this);
	}

}
