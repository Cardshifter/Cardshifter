package com.cardshifter.server.main;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.cardshifter.api.outgoing.ResetAvailableActionsMessage;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import org.junit.Assert;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestClient {

	private final Socket socket;
	private final ObjectMapper mapper;
	private final InputStream in;
	private final OutputStream out;
	private final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final Thread thread;
	
	public TestClient() throws UnknownHostException, IOException {
		this.socket = new Socket("127.0.0.1", 4242);
		this.mapper = CardshifterIO.mapper();
		out = socket.getOutputStream();
		in = socket.getInputStream();
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		thread = new Thread(this::listen);
		thread.start();
	}
	
	private void listen() {
		try {
			MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(in), Message.class);
			while (values.hasNext()) {
				Message msg = values.next();
				System.out.println("Incoming message " + msg);
				messages.offer(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void send(Message message) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(out, message);
	}

	public <T> List<T> awaitMany(int count, Class<T> class1) throws JsonParseException, JsonProcessingException, IOException, InterruptedException {
		List<T> result = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			result.add(await(class1));
		}
		assertEquals(count, result.size());
		return result;
	}
	
	public <T> T await(Class<T> class1) throws IOException, InterruptedException {
		Message message = messages.take();
		if (message instanceof ServerErrorMessage) {
			Assert.fail(message.toString());
		}
		if (!class1.isAssignableFrom(message.getClass())) {
			Assert.fail("Expected " + class1 + " but was " + message);
		}
		return class1.cast(message);
	}

	public void disconnect() throws IOException {
		socket.close();
		thread.interrupt();
	}

    public <T extends Message> T awaitUntil(Class<T> messageClass) throws InterruptedException, IOException {
        Message message;
        do {
            message = await(Message.class);
        } while (!messageClass.isAssignableFrom(message.getClass()));
        return messageClass.cast(message);
    }
}
