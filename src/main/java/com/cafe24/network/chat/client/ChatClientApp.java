package com.cafe24.network.chat.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp {
	
	private static final String SERVER_IP = "192.168.1.41";
	private static final int SERVER_PORT = 7000;

	public static void main(String[] args) {
		Socket socket = null;
		String name = null;
		Scanner scanner = new Scanner(System.in);

		while( true ) {
			
			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();
			
			if (name.isEmpty() == false ) {
				break;
			}
			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		
		try {
			socket = new Socket();
			
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			System.out.println("connected");
			
			new ChatWindow(name,socket).show();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(scanner != null) {
					scanner.close();
				}
				if( socket != null && socket.isClosed() == false) {
					socket.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
