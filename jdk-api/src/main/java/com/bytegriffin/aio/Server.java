package com.bytegriffin.aio;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	private static int port = 8765;
	//线程池
	private ExecutorService executorService;
	//线程组
	private AsynchronousChannelGroup threadGroup;
	//异步服务器通道
	public AsynchronousServerSocketChannel assc;

	public Server(int port) {
		try {
			executorService = Executors.newCachedThreadPool();//创建缓冲池
			threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);//创建线程组
			assc = AsynchronousServerSocketChannel.open(threadGroup);//创建服务器通道
			assc.bind(new InetSocketAddress(port));//进行绑定
			System.out.println("服务器已经启动，端口号为："+port);
			assc.accept(this, new ServerCompletionHandler());//阻塞
			Thread.sleep(Integer.MAX_VALUE);//不让服务器停止
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server(port);
	}

}
