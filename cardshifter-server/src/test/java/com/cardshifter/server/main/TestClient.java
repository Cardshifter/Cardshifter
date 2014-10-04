package com.cardshifter.server.main;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
	
	public TestClient() throws UnknownHostException, IOException {
		this.socket = new Socket("127.0.0.1", 4242);
		this.mapper = new ObjectMapper();
		out = socket.getOutputStream();
		in = socket.getInputStream();
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	}
	
	public void send(Message message) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(out, message);
	}

	public <T> List<T> awaitMany(int count, Class<T> class1) throws JsonParseException, JsonProcessingException, IOException {
		MappingIterator<T> values = mapper.readValues(new JsonFactory().createParser(in), class1);
		List<T> result = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			if (values.hasNext()) {
				result.add(values.next());
			}
		}
		assertEquals(count, result.size());
		return result;
	}
	
	public <T> T await(Class<T> class1) throws JsonParseException, JsonProcessingException, IOException {
		Message message = mapper.readValue(in, Message.class);
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
	}

}
