package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.cafe24.network.chat.util.ChatProtocol;

public class ChatServerClient extends Thread {

	private String nickname;
	private Socket socket;
	private List<ChatServerClient> listClient;

	public ChatServerClient(Socket socket, List<ChatServerClient> listClient) {
		this.socket = socket;
		this.listClient = listClient;
	}

	@Override
	public void run() {
		
		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
		int remotePort = inetRemoteSocketAddress.getPort();

		ChatServer.consoleLog("connected by client[" + remoteHostAddress + ":" + remotePort + "]");

		try {

			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(os, "utf-8"), true);

			while (true) {

				String request = br.readLine();

				if (request == null) {
					ChatServer.consoleLog("클라이언트로 부터 연결 끊김");
					doQuit();
					break;
				}
				dispatchChatRequest(request);
				ChatServer.consoleLog("received" + request);
			}

		} catch (SocketException e) {
			ChatServer.consoleLog("socket closed");
			doQuit();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			if (socket != null && socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
	}
	
	public String getNickname() {
		return nickname;
	}

	public Socket getSocket() {
		return socket;
	}

	public List<ChatServerClient> getListClient() {
		return listClient;
	}

	private void dispatchChatRequest(String request) {
		String[] tokens = request.split(":");
		String protocol = tokens[0];
		String context = tokens[1];
		switch (protocol) {
		case ChatProtocol.JOIN:
			doJoin(context);
			break;
		case ChatProtocol.MESSAGE:
			doMessage(context);
			break;
		default:
			ChatServer.consoleLog("에러:알수 없는 요청(" + protocol + ")");
			break;
		}
	}

	private void doQuit() {
		broadcast(ChatProtocol.MESSAGE, nickname + ChatProtocol.OUT_MESSAGE);
	}

	private void doMessage(String context) {
		String chatMessage = nickname + ">"+ context;
		broadcast(ChatProtocol.MESSAGE, chatMessage);
	}

	private void doJoin(String context) {
		this.nickname = context;
		broadcast(ChatProtocol.JOIN, context + ChatProtocol.JOIN_MESSAGE);
		synchronized (listClient) {
			listClient.add(this);
		}
		sendChatResponse(this.getSocket(), ChatProtocol.PERMIT, ChatProtocol.SUCCESS);
	}

	private void broadcast(String protocol, String message) {
		synchronized (listClient) {
			listClient.forEach((client) -> {
					sendChatResponse(client.getSocket(), protocol, message);
			});
		}
	}
	
	private PrintWriter convertToPrintWriter(Socket socket) {
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return printWriter;
	}
	
	private void sendChatResponse(Socket socket, String protocol, String message) {
		PrintWriter pr = convertToPrintWriter(socket);
		pr.println(protocol + ChatProtocol.COLON +message);
		pr.flush();
	}
}
