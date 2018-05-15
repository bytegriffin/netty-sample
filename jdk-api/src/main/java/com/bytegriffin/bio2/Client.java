package com.bytegriffin.bio2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	
	private static final String address = "127.0.0.1";
	private static final int port = 8765;
	
	public static void main(String[] args) {
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			socket = new Socket(address, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("这是客户端请求的数据，你好");
			String response = in.readLine();
			System.out.println("client say:"+response);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(in != null) {
					in.close();
				}
				if(out != null) {
					out.close();
				}
				if(socket != null) {
					socket.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
