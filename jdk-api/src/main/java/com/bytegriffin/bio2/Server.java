package com.bytegriffin.bio2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static int port = 8765;

	public static void main(String[] args) {
		ServerSocket server = null;
		Socket socket = null;
		try {
			server = new ServerSocket(port);
			System.out.println("服务器已经启动。。。");
			HandlerExecutorPool pool = new HandlerExecutorPool(50, 1000);
			while(true) {
				socket = server.accept();//进行阻塞
				pool.execute(new ServerHandler(socket));
			}
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
