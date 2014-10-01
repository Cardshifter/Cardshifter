package com.cardshifter.server.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.model.Server;

public class ClientSocketHandler extends ClientIO implements Runnable {
	private static final Logger logger = LogManager.getLogger(ClientSocketHandler.class);
	
	private Socket	socket;
	private final BufferedReader	in;
	private final PrintWriter	out;
	
	private final char[] readBuffer = new char[4096];

	public ClientSocketHandler(Server server, Socket socket) throws IOException {
		super(server);
		this.socket = socket;
		
		
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
		
	}
	
	@Override
	public void onSendToClient(String message) {
		this.out.print(message);
		this.out.flush();
	}

	@Override
	public void run() {
		String data = "";
		
		int eventNr = 0;
		while (socket != null && socket.isConnected()) {
			try {
				int bytesRead = 0;
				while ((bytesRead = in.read(readBuffer)) != -1) {
					data = new String(readBuffer, 0, bytesRead);
					logger.info("");
					
					String[] datas = data.split("" + (char) 0);
					for (String mess : datas) {
						if (mess.trim().isEmpty())
							continue;
						
						logger.info("[Event #" + ++eventNr + "]");
						logger.info("Received from " + this + ": " + mess);
						this.sentToServer(mess);
					}
				}
				logger.info("Socket Communication no more bytes to read for " + this);
				this.disconnected();
				if (socket != null) {
					socket.close();
				}
				socket = null;
			} catch (IOException ioe) {
				logger.warn("Socket " + this + " exception: " + ioe.getMessage());
				this.disconnected();
				try {
					if (this.socket != null) {
						this.socket.close();
					}
				} catch (IOException e) {
				}
				this.socket = null;
				logger.debug("Socket has been set to null: " + this);
			}
		}
		logger.info("End of run method for " + this);
	}

	@Override
	public void close() {
		try {
			socket.close();
		}
		catch (IOException e) {
			logger.warn("Error closing", e);
		}
		this.disconnected();
	}
}
