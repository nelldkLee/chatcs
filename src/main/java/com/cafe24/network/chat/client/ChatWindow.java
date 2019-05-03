package com.cafe24.network.chat.client;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;

import com.cafe24.network.chat.util.ChatProtocol;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private String name;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	private Socket socket;

	public ChatWindow(String name, Socket socket) throws UnsupportedEncodingException, IOException {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		this.name = name;
		this.socket = socket;
		bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
		printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
		new ChatClient(bufferedReader).start();
	}

	public void show() throws UnsupportedEncodingException, IOException {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER && textField.getText().trim().length() > 1) {
					sendMessage();
					textField.setText("");
				}
			}

		});
		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		frame.pack();
	}

	public void updateTextArea(String message) {
		textArea.append(message);
		textArea.append("\n");

	}

	public void sendMessage() {
		String message = textField.getText();
		sendChatRequest(ChatProtocol.MESSAGE, message);
	}

	public void sendChatRequest(String protocol, String message) {
		printWriter.println(protocol + ChatProtocol.COLON + message);
	}

	public class ChatClient extends Thread {

		private BufferedReader bufferedReader;

		public ChatClient(BufferedReader bufferedReader) {
			this.bufferedReader = bufferedReader;
		}

		public void run() {
			try {
				networking();
			} catch (SocketException e) {
				e.printStackTrace();
				updateTextArea("서버 연결이 끊겼습니다.");
			} catch (IOException e) {
				System.out.println("서버 연결이 끊겼습니다.");
			} finally {
				destroyedChat();
			}
		}

		public void networking() throws IOException {
			sendChatRequest(ChatProtocol.JOIN, name);

			while (true) {

				String response = bufferedReader.readLine();
				dispatchChatResponse(response);
				if (response == null) {
					doQuit();
					break;
				}
			}
		}

		public void dispatchChatResponse(String response) {
			String[] tokens = response.split(":");
			String protocol = tokens[0];
			String context = tokens[1];
			System.out.println("protocol" + protocol);
			switch (protocol) {
			case ChatProtocol.JOIN:
				doJoin(context);
				break;
			case ChatProtocol.MESSAGE:
				doMessage(context);
				break;
			case ChatProtocol.P2P:
				doP2P(context);
				break;
			case ChatProtocol.QUIT:
				doQuit();
				break;
			case ChatProtocol.PERMIT:
				doPermit();
				break;
			default:
				System.out.println("에러:알수 없는 요청(" + protocol + ")");
				break;
			}
		}

		private void doP2P(String context) {
			String[] temp = context.split(">");
			String[] p2p = temp[0].split("@");
			if (name.equals(p2p[0])) {
				context = p2p[1] + "님에게 귓속말: " + temp[1];
			} else {
				context = p2p[0] + "님의 귓속말: " + temp[1];
			}
			updateTextArea(context);
		}

		private void doPermit() {
			updateTextArea("채팅 방에 입장하셨습니다.");
		}

		private void doQuit() {
			updateTextArea("방이 종료되었습니다.!!!!!!!");
		}

		private void doMessage(String context) {
			String[] temp = context.split(">");
			String messageName = temp[0];
			if (name.equals(messageName)) {
				context = "나>>>" + temp[1];
			}
			updateTextArea(context);
		}

		private void doJoin(String context) {
			updateTextArea(context);
		}

		private void destroyedChat() {
			if (socket != null && socket.isClosed() == false) {
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
