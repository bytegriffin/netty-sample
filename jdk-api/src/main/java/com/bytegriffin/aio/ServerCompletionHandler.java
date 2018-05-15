package com.bytegriffin.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

public class ServerCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Server>{

	/**
	 * 当有多个客户端接入进来时都可以阻塞
	 */
	@Override
	public void completed(AsynchronousSocketChannel result, Server attachment) {
		attachment.assc.accept(attachment, this);
		read(result);
	}

	@Override
	public void failed(Throwable exc, Server attachment) {
		exc.printStackTrace();
	}

	private void read(final AsynchronousSocketChannel asc) {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		asc.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
			@Override
			public void completed(Integer resultSize, ByteBuffer attachment) {
				attachment.flip();//进行复位
				String resultData = new String(attachment.array()).trim();
				System.out.println("Server says:" + "收到客户端的数据信息为:" + resultData);
				String response = "服务器响应, 收到了客户端发来的数据: " + resultData;
				write(asc, response);
			}
			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				exc.printStackTrace();
			}
		});
	}

	private void write(AsynchronousSocketChannel asc, String response) {
		try {
			ByteBuffer buf = ByteBuffer.allocate(1024);
			buf.put(response.getBytes());
			buf.flip();
			asc.write(buf).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}
