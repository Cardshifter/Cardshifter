package com.cardshifter.server.messages;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.junit.Test;

import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.server.clients.ByteTransformer;

public class ByteSerializeTest {
	
	@Test
	public void testName() throws Exception {
		ByteTransformer transformer = new ByteTransformer();
		byte[] result = transformer.transform(new LoginMessage("BUBU"));
		System.out.println(Arrays.toString(result));
		assertArrayEquals(new byte[]{ 0, 0, 0, 26, 0, 0, 0, 5, 0, 108, 0, 111, 0, 103, 0, 105, 0, 110, 0, 0, 0, 4, 0, 66, 0, 85, 0, 66, 0, 85 }, result);
		
		Message message = transformer.readOnce(new ByteArrayInputStream(result));
		assertTrue(message instanceof LoginMessage);
		assertEquals("BUBU", ((LoginMessage) message).getUsername());
	}

}
