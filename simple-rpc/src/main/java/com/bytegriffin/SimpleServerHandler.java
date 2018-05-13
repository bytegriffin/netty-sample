package com.bytegriffin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 服务端消息处理
 * @author bytegriffin
 *
 */
public class SimpleServerHandler extends ChannelInboundHandlerAdapter {
	
	public void channelRead(ChannelHandlerContext ctx,Object msg) {
		//读取消息
		ByteBuf result = (ByteBuf)msg;
		byte[] array = new byte[result.readableBytes()];
		//解码：将数据读取到byte[]中
		result.readBytes(array);
		String resultStr = new String(array);
		System.out.println("客户端发来的消息："+resultStr);
		result.release();
		//发送消息
		String response = "你也好，客户端，我是服务端。";
		//编码：将byte[]转换为ByteBuf对象
		ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
		encoded.writeBytes(response.getBytes());
		ctx.write(encoded);
		ctx.flush();
	}
	
	/**
	 * 当出现异常就关闭连接  
	 */
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {  
        cause.printStackTrace();  
        ctx.close();  
    }  
  
    @Override  
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {  
        ctx.flush();  
    }  

}
