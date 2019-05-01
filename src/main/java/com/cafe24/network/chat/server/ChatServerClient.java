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
			// 4. IOStream 받아오기
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(os, "utf-8"), true);

			while (true) {
				// 5. 데이터 읽기
				String request = br.readLine();

				if (request == null) {
					ChatServer.consoleLog("클라이언트로 부터 연결 끊김");
					break;
				}
				dispatchRequest(request);
				ChatServer.consoleLog("received" + request);
			}

		} catch (SocketException e) {
			ChatServer.consoleLog("sudden closed");

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

	private void dispatchRequest(String request) {
		String[] tokens = request.split(":");
		String protocol = tokens[0];
		String context = tokens[1];
		switch (tokens[0]) {
		case ChatProtocol.JOIN:
			doJoin(context);
			break;
		case ChatProtocol.MESSAGE:
			doMessage(context);
			break;
		case ChatProtocol.QUIT:
			doQuit();
			break;
		default:
			ChatServer.consoleLog("에러:알수 없는 요청(" + protocol + ")");
			break;
		}
	}

	private void doQuit() {

	}

	private void doMessage(String context) {

	}

	private void doJoin(String context) {
		this.nickname = context;
		String joinMessage = nickname + "님이 참여하였습니다.";
		broadcast(joinMessage);
		synchronized (listClient) {
			listClient.add(this);
		}
		PrintWriter printWriter = convertToPrintWriter(this.getSocket());
		printWriter.println("join:ok");
		printWriter.flush();
	}

	private void broadcast(String joinMessage) {
		synchronized (listClient) {
			listClient.forEach((client) -> {
					PrintWriter pr = convertToPrintWriter(client.getSocket());
					pr.println(joinMessage);
					pr.flush();
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
}
