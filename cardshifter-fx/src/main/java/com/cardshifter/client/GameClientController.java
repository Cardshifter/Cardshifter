package com.cardshifter.client;

import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameClientController {
	//private final Socket socket;
	//private final InputStream in;
	//private final OutputStream out;
	private final ObjectMapper mapper = new ObjectMapper();
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final List<UseableActionMessage> actions = Collections.synchronizedList(new ArrayList<>());
	private int gameId;
	
	public void acceptIPAndPort(String ipAddress, int port) {
		System.out.println(ipAddress);
	}
}
