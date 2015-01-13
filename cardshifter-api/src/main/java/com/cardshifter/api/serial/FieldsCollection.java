package com.cardshifter.api.serial;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.messages.Message;

public class FieldsCollection<T> {

	private static final Logger logger = LogManager.getLogger(FieldsCollection.class);
	
	private final List<Field> fields;

	public FieldsCollection(List<Field> fields) {
		this.fields = Collections.unmodifiableList(fields);
	}

	public static <T> FieldsCollection<T> gather(T object) {
		List<Field> fields = new ArrayList<Field>();
		Class<?> clazz = object.getClass();
		while (clazz != null) {
			addFields(fields, clazz);
			clazz = clazz.getSuperclass();
		}
		return new FieldsCollection<T>(fields);
	}

	private static void addFields(List<Field> fields, Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers())) {
				fields.add(field);
			}
		}
	}
	
	public byte[] serialize(T message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		try {
			for (Field field : fields) {
				serialize(field, message, out);
			}
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		byte[] data = baos.toByteArray();
		
		baos = new ByteArrayOutputStream();
		out = new DataOutputStream(baos);
		out.writeInt(data.length);
		baos.write(data);
		return baos.toByteArray();
	}

	private Object deserialize(Class<?> type, DataInputStream data) throws IOException {
		logger.debug("deserialize " + type);
		if (type == int.class || type == Integer.class) {
			return (Integer) data.readInt();
		}
		else if (type == String[].class) {
			int count = data.readInt();
			String[] str = new String[count];
			logger.debug("String array length " + count);
			for (int i = 0; i < str.length; i++) {
				str[i] = (String) deserialize(String.class, data);
				logger.debug("String read " + i + ": " + str[i]);
			}
			return str;
		}
		else if (type == String.class) {
			int length = data.readInt();
			logger.debug("String length " + length);
			StringBuilder str = new StringBuilder(length);
			for (int i = 0; i < length; i++) {
				str.append(data.readChar());
			}
			return str.toString();
		}
		else if (Enum.class.isAssignableFrom(type)) {
			Object[] values = type.getEnumConstants();
			int ordinal = data.readInt();
			return values[ordinal];
		}
		else {
			throw new IOException("unknown type " + type);
		}
	}
	
	private void deserialize(Field field, Message message, DataInputStream data) throws IllegalArgumentException, IllegalAccessException, IOException {
		logger.debug("read field " + field + " for " + message);
		field.setAccessible(true);
		Class<?> type = field.getType();
		field.set(message, deserialize(type, data));
	}

	private void serialize(Class<?> type, Object value, DataOutputStream out) throws IOException, IllegalArgumentException, IllegalAccessException {
		if (type == int.class || type == Integer.class) {
			out.writeInt((Integer) value);
		}
		else if (type == String.class) {
			String str = (String) value;
			out.writeInt(str.length());
			out.writeChars(str);
		}
		else if (type == String[].class) {
			String[] arr = (String[]) value;
			out.writeInt(arr.length);
			for (int i = 0; i < arr.length; i++) {
				serialize(String.class, arr[i], out);
			}
		}
		else if (Enum.class.isAssignableFrom(type)) {
			Enum<?> enumValue = (Enum<?>) value;
			out.writeInt(enumValue.ordinal());
		}
		else {
			throw new IOException("unknown type " + type);
		}
	}
	
	private void serialize(Field field, T obj, DataOutputStream out) throws IOException, IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		Class<?> type = field.getType();
		Object value = field.get(obj);
		serialize(type, value, out);
	}

	public FieldsCollection<T> orderByName() {
		List<Field> myFields = new ArrayList<Field>(fields);
		Collections.sort(myFields, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return new FieldsCollection<T>(myFields);
	}

	public FieldsCollection<T> putFirst(String fieldName) {
		List<Field> myFields = new ArrayList<Field>(fields);
		for (Field field : myFields) {
			if (field.getName().equals(fieldName)) {
				myFields.remove(field);
				myFields.add(0, field);
				return new FieldsCollection<T>(myFields);
			}
		}
		throw new IllegalArgumentException("Field name not found: " + fieldName);
	}

	public void read(Message message, DataInputStream data) throws IOException {
		try {
			for (Field field : fields) {
				deserialize(field, message, data);
			}
		}
		catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	public FieldsCollection<T> skipFirst() {
		List<Field> myFields = new ArrayList<Field>(fields);
		myFields.remove(0);
		return new FieldsCollection<T>(myFields);
	}

}
