package com.bytegriffin;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;

public class Session {

    static final int READ_IDLE_TIME_OUT = 0;  
    static final int WRITE_IDLE_TIME_OUT = 0;  
    static final int IDLE_TIME_OUT = 0;  
    static final String server_ip = "127.0.0.1";
    static final int server_port = 8080;

    static List<String> register_ips = new ArrayList<String>();

    static {
    	registerIP();
    }

    static void registerIP(){
    	register_ips.add("127.0.0.1");
    }

    /**
     * 验证客户端ip是否合法
     * @param ip
     * @return
     */
    static boolean authIP(String ip){
    	return register_ips.contains(ip);
    }

    /**
     * 获取ip
     * @param ctx
     * @return
     */
    static String getIP(ChannelHandlerContext ctx){
    	String address = ctx.channel().remoteAddress().toString();
		address = address.replace("/", "");
		if(address.contains(":")){
			address = address.split(":")[0]; 
		}
		return address;
    }

}
