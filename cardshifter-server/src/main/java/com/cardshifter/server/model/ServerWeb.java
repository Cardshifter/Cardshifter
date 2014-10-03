package com.cardshifter.server.model;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.cardshifter.server.clients.ClientWebSocket;

public class ServerWeb implements ConnectionHandler {
	private static final Logger logger = LogManager.getLogger(ServerWeb.class);
	
	private final InnerServer websocketServer;
	
	public ServerWeb(Server server, int port) {
		this.websocketServer = new InnerServer(server, port);
	}

	private static class InnerServer extends WebSocketServer {
		
		public InnerServer(Server server, int port) {
			super(new InetSocketAddress(port));
			this.webClients = new ConcurrentHashMap<>();
			this.server = server;
		}
		
		private final Map<WebSocket, ClientIO> webClients;
		private final Server server;
		
		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			logger.info("Connection opened: " + conn);
			ClientIO io = new ClientWebSocket(server, conn);
			webClients.put(conn, io);
			server.newClient(io);
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			logger.info("Connection closed: " + conn + " code " + code + " reason " + reason + " remote " + remote);
			ClientIO io = webClients.remove(conn);
			if (io == null) {
				logger.error("Closing unknown ClientIO");
				return;
			}
			io.close();
			server.onDisconnected(io);
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			logger.info("Connection message from: " + conn + ": " + message);
			ClientIO io = webClients.get(conn);
			if (io == null) {
				logger.error("Message was recieved from unknown ClientIO");
				return;
			}
			io.sentToServer(message);
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			logger.warn("Connection error: " + conn, ex);
		}
		
	}

	@Override
	public void start() {
		logger.info("Starting WebSocket server at port " + websocketServer.getPort() + "...");
		websocketServer.start();
		logger.info("WebSocket server started");
	}

	@Override
	public void shutdown() throws Exception {
		logger.info("Shutting down WebSocket server...");
		websocketServer.stop();
		logger.info("WebSocket server shutdown.");
	}
	
}
