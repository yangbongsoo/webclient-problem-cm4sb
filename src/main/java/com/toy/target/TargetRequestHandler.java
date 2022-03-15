package com.toy.target;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetRequestHandler implements Runnable {

	private final static Logger log = LoggerFactory.getLogger(TargetRequestHandler.class);
	private Socket connection;

	public TargetRequestHandler(Socket connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		log.debug("New client! Connected IP : {}, port : {}", connection.getInetAddress(), connection.getPort());
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

			// request
			DataInputStream dis = new DataInputStream(in);
			byte[] byteArr = new byte[25000];
			int readByteCount = dis.read(byteArr);
			String data = new String(byteArr, 0, readByteCount, StandardCharsets.UTF_8);
			log.info("request data : {}", data);

			// response
			DataOutputStream dos = new DataOutputStream(out);
			byte[] responseBody = "Hello I am Target".getBytes();
			response200Header(dos, responseBody.length);
			responseBody(dos,responseBody);
		} catch (IOException e) {
			// ignore
		}
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/plain;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
