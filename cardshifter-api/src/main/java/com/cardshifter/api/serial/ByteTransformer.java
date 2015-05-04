package com.cardshifter.api.serial;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.cardshifter.api.CardshifterSerializationException;
import com.cardshifter.api.LogInterface;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.messages.MessageTypeIdResolver;

public class ByteTransformer implements CommunicationTransformer {

    private final LogInterface logger;
    private final ReflectionInterface refl;

    public ByteTransformer(LogInterface logger, ReflectionInterface refl) {
        this.logger = logger;
        this.refl = refl;
    }

    public byte[] transform(Message message) throws CardshifterSerializationException {
		FieldsCollection<Message> fields = FieldsCollection.gather(message, logger, refl);
		fields = fields.orderByName().putFirst("command");
		return fields.serialize(message);
	}
	
	@Override
	public void send(Message message, OutputStream out) throws CardshifterSerializationException {
		// 1. find Fields to serialize
		// 2. order Fields, pre-process
		// 3. serialize
		logger.info("byte send " + message);
		FieldsCollection<Message> fields = FieldsCollection.gather(message, logger, refl);
		fields = fields.orderByName().putFirst("command");
		byte[] b = fields.serialize(message);
		logger.info("byte send " + Arrays.toString(b));
        try {
            out.write(b);
        } catch (IOException e) {
            throw new CardshifterSerializationException(e);
        }
    }

	@Override
	public void read(InputStream in, MessageHandler onReceived) throws CardshifterSerializationException {
		try {
			logger.info("Started reading " + this);
			Message message = readOnce(in);
			logger.info("byte recieve " + message);
			if (!onReceived.messageReceived(message)) {
				return;
			}
		} catch (Exception e) {
			throw new CardshifterSerializationException(e);
		}
	}

	public Message readOnce(InputStream in) throws CardshifterSerializationException {
        try {
            DataInputStream data = new DataInputStream(in);
            int numBytes = data.readInt();
            logger.info("bytes received " + numBytes);
            byte[] actualData = new byte[numBytes];

            int read = 0;
            while (read < numBytes) {
                read += data.read(actualData, read, numBytes - read);
            }

            logger.info("bytes data received " + Arrays.toString(actualData));
            data = new DataInputStream(new ByteArrayInputStream(actualData));

            int typeLength = data.readInt();
            String str = readString(data, typeLength);
            System.out.println(str);
            Class<?> type = MessageTypeIdResolver.get(str);
            Message message;
            try {
                message = (Message) refl.create(type);
            } catch (Exception e) {
                throw new CardshifterSerializationException(e);
            }
            FieldsCollection<Message> fields = FieldsCollection.gather(message, logger, refl);
            fields = fields.orderByName().putFirst("command").skipFirst();
            fields.read(message, data);
            return message;
        } catch (IOException ex) {
            throw new CardshifterSerializationException(ex);
        }
	}

	private String readString(DataInputStream data, int numBytes) throws IOException {
		StringBuilder str = new StringBuilder(numBytes);
		for (int i = 0; i < numBytes; i++) {
			str.append(data.readChar());
		}
		return str.toString();

/*		byte[] bytes = new byte[numBytes * 2];
		data.read(bytes);
		return new String(bytes, StandardCharsets.UTF_16);*/
	}

}
