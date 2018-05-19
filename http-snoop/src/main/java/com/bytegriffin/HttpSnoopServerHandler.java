package com.bytegriffin;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

public class HttpSnoopServerHandler extends SimpleChannelInboundHandler<Object> {

	private HttpRequest request;

	//response返回的内容buffer
	private final StringBuilder buf = new StringBuilder();

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;
			if (HttpUtil.is100ContinueExpected(request)) {
				send100Continue(ctx);
			}

			buf.setLength(0);
			buf.append("=================Http Server==================\r\n");

			buf.append("VERSION: ").append(request.protocolVersion()).append("\r\n");
			buf.append("HOSTNAME: ").append(request.headers().get(HttpHeaderNames.HOST, "unknown")).append("\r\n");
			buf.append("REQUEST_URI: ").append(request.uri()).append("\r\n\r\n");

			HttpHeaders headers = request.headers();
			if (!headers.isEmpty()) {
				for (Map.Entry<String, String> h : headers) {
					CharSequence key = h.getKey();
					CharSequence value = h.getValue();
					buf.append("HEADER: ").append(key).append(" = ").append(value).append("\r\n");
				}
				buf.append("\r\n");
			}

			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
			Map<String, List<String>> params = queryStringDecoder.parameters();
			if (!params.isEmpty()) {
				for (Entry<String, List<String>> p : params.entrySet()) {
					String key = p.getKey();
					List<String> vals = p.getValue();
					for (String val : vals) {
						buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
					}
				}
				buf.append("\r\n");
			}
			appendDecoderResult(buf, request);
		}

		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;
			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				buf.append("CONTENT: ");
				buf.append(content.toString(CharsetUtil.UTF_8));
				buf.append("\r\n");
				appendDecoderResult(buf, request);
			}
			if (msg instanceof LastHttpContent) {
				buf.append("END OF CONTENT\r\n");
				LastHttpContent trailer = (LastHttpContent) msg;
				if (!trailer.trailingHeaders().isEmpty()) {
					buf.append("\r\n");
					for (CharSequence name : trailer.trailingHeaders().names()) {
						for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
							buf.append("TRAILING HEADER: ");
							buf.append(name).append(" = ").append(value).append("\r\n");
						}
					}
					buf.append("\r\n");
				}
				// 在服务端关闭连接
				if (!writeResponse(trailer, ctx)) {
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
	}

	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.decoderResult();
		if (result.isSuccess()) {
			return;
		}
		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpUtil.isKeepAlive(request);
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				currentObj.decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
				Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}

		String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
			if (!cookies.isEmpty()) {
				for (Cookie cookie : cookies) {
					response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
				}
			}
		} else {
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
		}
		
		ctx.write(response);
		return keepAlive;
	}
	
	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		ctx.write(response);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
