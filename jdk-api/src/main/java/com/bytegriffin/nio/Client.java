package com.bytegriffin.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
	
	private static final String host = "127.0.0.1";
	private static final int port = 8765;
	
	public static void main(String[] args) {
		InetSocketAddress address = new InetSocketAddress(host, port);//创建连接的地址
		SocketChannel sc = null;//声明连接通道
		ByteBuffer buf = ByteBuffer.allocate(1024);//建立缓冲区
		try {
			sc = SocketChannel.open();//打开通道
			sc.connect(address);//建立连接
			while(true) {
				byte[] bytes = new byte[1024];
				System.in.read(bytes);
				buf.put(bytes);//把数据放到缓冲区中
				buf.flip();//对缓冲区进行复位
				sc.write(buf);//写入数据
				buf.clear();//清空缓冲区数据
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(sc != null){
				try {
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
