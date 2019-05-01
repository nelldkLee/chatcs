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
import java.nio.charset.StandardCharsets;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket;
	private String name;
	
	public ChatWindow(String name, Socket socket) {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		this.socket = socket;
		this.name = name;
	}

	public void show() throws UnsupportedEncodingException, IOException {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_ENTER) {
					
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
		running();
	}
	
	private void running() throws UnsupportedEncodingException, IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream() , "utf-8"));
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
		printWriter.println("join:" + name);
		printWriter.flush();
		updateTextArea("채팅 방에 입장하셨습니다.");
		
		while(true) {
			String response = bufferedReader.readLine();
			dispatchResponse(response);
			if(response == null) {
				updateTextArea("방이 종료되었습니다.!!!!!!!");
				break;
			}
			
		}
		
	}
	
	private void dispatchResponse(String response) {
		// TODO Auto-generated method stub
		
	}

	private void updateTextArea(String message) {
		textArea.append(message);
		textArea.append("\n");
	}
	
	private void sendMessage() {
		String message = textField.getText();
	}
	
	private PrintWriter convertToPrintWriter(Socket socket) {
		PrintWriter pr = null;
		try {
			pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pr;
	}
}
