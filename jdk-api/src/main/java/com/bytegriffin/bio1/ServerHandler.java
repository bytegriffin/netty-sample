package com.bytegriffin.bio1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerHandler implements Runnable{

	private Socket socket;

	public ServerHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(), true);
			String body = null;
			while(true) {
				body = in.readLine();
				if(body == null) {
					break;
				}
				System.out.println("Server say:"+body);
				out.println("这是服务器端的响应数据，你也好");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(in != null) {
					in.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
