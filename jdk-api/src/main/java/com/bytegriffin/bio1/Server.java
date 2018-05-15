package com.bytegriffin.bio1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static int port = 8765;

	public static void main(String[] args) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("服务器已经启动。。。");
			//进行阻塞
			Socket socket = server.accept();
			//来一个请求new一个线程处理
			new Thread(new ServerHandler(socket)).start();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(server != null) {
					server.close();
				}
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}
