package com.cardshifter.server.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.cardshifter.server.main.ServerConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientSocketHandler;

public class ServerSock implements ConnectionHandler {
	private static final Logger logger = LogManager.getLogger(ServerSock.class);
	
	private final AtomicInteger activeConnections = new AtomicInteger(0);
	private final AtomicInteger threadCounter = new AtomicInteger(0);
	private final ExecutorService executor;
	private final Server server;
	private final Thread thread;
	private final ServerSocket serverSocket;

	/**
	 * Constructor.
	 * @param server Server instance
	 * @param config Uses the value of {@code config.getPortSocket} as port. If {@code port == 0} any available port is
	 *                  used and the real port number is set in {@code config} before returning.
	 * @throws IOException
	 */
	public ServerSock(Server server, ServerConfiguration config) throws IOException {
		this.server = server;
		this.executor = Executors.newCachedThreadPool(r -> new Thread(r, "Conn-" + threadCounter.getAndIncrement()));

		// If port = 0, use any open port. Set the config port to the real port used.
		this.serverSocket = new ServerSocket(config.getPortSocket());
		config.setPortSocket(serverSocket.getLocalPort());

		this.thread = new Thread(this::run);
	}

	private void run() {
		try {
			int maxConnections = 0;
			
			while (activeConnections.incrementAndGet() < maxConnections || maxConnections == 0) {
				logger.info("Waiting for client nr: " + activeConnections.get() + "...");
				Socket client = serverSocket.accept();
				ClientSocketHandler clientHandler = new ClientSocketHandler(this.server, client);
				logger.info("Incoming connection from " + client.getRemoteSocketAddress());
				if (thread.isInterrupted()) {
					logger.info("ServerSocket thread interrupted, shutting down.");
					break;
				}
				this.server.newClient(clientHandler);
				Future<?> future = executor.submit(clientHandler);
			}
		}
		catch (Exception e) {
			if(e.getCause() instanceof SocketException) {
				logger.info("SocketException has happened in the server, the socket for the server is closed");
			} else {
				logger.error("Error in ServerSocket", e);
			}
		}
	}


	@Override
	public void start() {
		logger.info("Starting Socket server at port " + serverSocket.getLocalPort() + "...");
		thread.start();
		logger.info("Socket server started.");
	}

	@Override
	public void shutdown() {
		thread.interrupt();
		logger.info("Shutting down ServerSock Executor");
		executor.shutdownNow();
		logger.info("Shutting down serverSocket");
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.error("IOException when closing ServerSocket", e);
		}
	}

}
