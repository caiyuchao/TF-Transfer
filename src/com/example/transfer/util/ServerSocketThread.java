package com.example.transfer.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketThread extends ServerSocket implements Runnable{
	
	public ServerSocketThread(int port) throws IOException {
		super(port);
	}

	@Override
	public void run() {
		while(true){
			try {
				Socket socket = accept();
				System.out.println("有客户连接");
				MyServerThread myServerThread = new MyServerThread(socket);
				myServerThread.start();
				SocketThreadFactory.addClient(myServerThread);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				break;
			}
		}
	}

}
