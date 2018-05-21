package com.bytegriffin;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 服务端处理类
 */
@ChannelHandler.Sharable  
public class RpcServerHandler extends SimpleChannelInboundHandler<Message.Response>  {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String ip = Session.getIP(ctx);
		if(!Session.authIP(ip)){
			System.out.println("服务端发现有非法客户端["+ip+"]接入连接。");
			ctx.close();
		}else {
			System.out.println("服务端发现有新客户端["+ip+"]接入连接。");
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		System.out.println("服务端发现有客户端["+Session.getIP(ctx)+"]断开连接。");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message.Response msg) throws Exception {
		System.out.println("服务端接收到客户端[" + Session.getIP(ctx) + "]发来为["+msg.getRequestId()+"]的数据。");
		// 处理客户端数据并回写给客户端
		ctx.writeAndFlush(response(msg.getRequestId()));
		// 通知执行下一个InboundHandler
		// ctx.fireChannelRead(msg);
	}

	/**
	 * 构建Message.Response实体类
	 * @param requestId
	 * @return
	 */
	private Message.Response response(String requestId) {
		Message.Response.Builder builder = Message.Response.newBuilder();
		builder.setRequestId(requestId);
		builder.setStatus("200");
		builder.setDesc("成功接收并返回。");
		return builder.build();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.err.println("服务端发现异常："+ cause);
		ctx.close();
	}

	/**
	 * 心跳机制：用于保活或断线处理
	 * 目前是服务端主动发送心跳给客户端
	 */
	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
            	System.err.println("读超时");
            } else if (event.state() == IdleState.WRITER_IDLE) {
            	System.err.println("写超时");
            } else if (event.state() == IdleState.ALL_IDLE) {
            	System.err.println("总超时");
            }
        } else {
			super.userEventTriggered(ctx, evt);
		}
    }

}