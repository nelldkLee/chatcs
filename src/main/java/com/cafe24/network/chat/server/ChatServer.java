package com.cafe24.network.chat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	
	private static final int PORT = 8888;
	private static Map<String, ChatServerClient> listClient = new HashMap<String, ChatServerClient>();

	public static void main(String[] args) {

		ServerSocket serverSocket = null;

		try {
			// 1. Create Server Socket
			serverSocket = new ServerSocket();
			   
			// 2. Bind
			serverSocket.bind( new InetSocketAddress( "0.0.0.0", PORT ) );
			consoleLog("httpd starts at " + PORT);

			while(true) {
				// 3. Wait for connecting ( accept )
				Socket socket = serverSocket.accept();
				// 4. Delegate Processing Request
				new ChatServerClient(socket, listClient).start();
				
			}

		} catch (IOException ex) {
			consoleLog("error:" + ex);
		} finally {
			// 5. 자원정리
			try {
				if (serverSocket != null && serverSocket.isClosed() == false) {
					serverSocket.close();
				}
			} catch (IOException ex) {
				consoleLog("error:" + ex);
			}
		}
	}

	public static void consoleLog(String message) {
		System.out.println("[HttpServer#" + Thread.currentThread().getId()  + "] " + message);
	}
	
}
