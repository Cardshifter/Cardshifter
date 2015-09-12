package com.cardshifter.server.model;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cardshifter.api.CardshifterSerializationException;
import com.cardshifter.api.serial.ByteTransformer;
import com.cardshifter.server.main.ServerConfiguration;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.cardshifter.api.ClientIO;
import com.cardshifter.server.clients.ClientWebSocket;

public class ServerWeb implements ConnectionHandler {
	private static final Logger logger = LogManager.getLogger(ServerWeb.class);
	
	private final InnerServer websocketServer;

	/**
	 * Constructor.
	 * @param server Server instance
	 * @param config Uses the value of {@code config.getPortWebsocket} as port. If {@code port == 0} any available port
	 *                  is used and the real port number is set in {@code config} before returning.
	 */
	public ServerWeb(Server server, ServerConfiguration config) {
		this.websocketServer = new InnerServer(server, config.getPortWebsocket());
		config.setPortWebsocket(websocketServer.getPort());
	}

	private static class InnerServer extends WebSocketServer {

        private final ByteTransformer transformer = CardshifterIO.createByteTransformer();

        public InnerServer(Server server, int port) {
			super(new InetSocketAddress(port));
			this.webClients = new ConcurrentHashMap<>();
			this.server = server;
		}
		
		private final Map<WebSocket, ClientWebSocket> webClients;
		private final Server server;
		
		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			logger.info("Connection opened: " + conn);
			ClientWebSocket io = new ClientWebSocket(server, conn);
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
            try {
                ClientWebSocket client = (ClientWebSocket) io;
                client.handleMessage(message);
            } catch (CardshifterSerializationException e) {
                throw new RuntimeException(e);
            }
//			io.sentToServer(message);
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
