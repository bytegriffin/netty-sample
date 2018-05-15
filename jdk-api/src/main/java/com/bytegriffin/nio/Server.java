package com.bytegriffin.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server implements Runnable{
	
	private final static int port = 8765;

	private Selector selector;//多路复用器

	private ByteBuffer readBuf = ByteBuffer.allocate(1024);//建立缓冲区
	
	//private ByteBuffer writeBuf = ByteBuffer.allocate(1024);//建立缓冲区
	
	public Server(int port) {
		try {
			this.selector = Selector.open();//打开多路复用器
			ServerSocketChannel ssc = ServerSocketChannel.open();//打开服务器通道
			ssc.configureBlocking(false);//设置为非阻塞模式
			ssc.bind(new InetSocketAddress(port));//绑定地址
			ssc.register(this.selector, SelectionKey.OP_ACCEPT);//把服务器通道注册到多路复用器上，并监听阻塞通道
			System.out.println("服务器已经启动，端口为："+port);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				this.selector.select();//必须要让多路复用器开始监听
				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();//返回多路复用器已经选择的结果集
				while(keys.hasNext()) {
					SelectionKey key= keys.next();//轮训获取下一个的SelectionKey元素
					keys.remove();//直接从容器中删除
					if(key.isValid()) {//如果有效
						if(key.isAcceptable()) {//如果为阻塞状态
							this.accept(key);
						}
						if(key.isReadable()) {//如果为可读状态
							this.read(key);
						}
						if(key.isWritable()) {//如果为可写状态
							this.write(key);
						}
					}
				}
						
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel ssc = (ServerSocketChannel) key.channel();//获取服务端通道
			SocketChannel sc = ssc.accept();//获取客户端通道
			sc.configureBlocking(false);//设置阻塞方法
			sc.register(this.selector, SelectionKey.OP_READ);//将客户端通道注册到多路复用器上，并标志为读取状态
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void write(SelectionKey key){
		//ServerSocketChannel ssc =  (ServerSocketChannel) key.channel();
		//ssc.register(this.seletor, SelectionKey.OP_WRITE);
	}

	private void read(SelectionKey key) {
		try {
			this.readBuf.clear();//清空缓冲区旧数据
			SocketChannel sc = (SocketChannel) key.channel();//获取之前注册的客户端通道对象
			int count = sc.read(this.readBuf);//读取数据
			if(count == -1) {//如果没有数据
				key.channel().close();
				key.cancel();
				return;
			}
			this.readBuf.flip();//读取之前将数据进行复位操作
			byte[] bytes = new byte[this.readBuf.remaining()];//根据缓冲区的数据长度创建相应大小的byte数组
			this.readBuf.get(bytes);//接收缓冲区数据
			String body = new String(bytes).trim();
			System.out.println("Server says:"+body);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Thread(new Server(port)).start();
	}

}
