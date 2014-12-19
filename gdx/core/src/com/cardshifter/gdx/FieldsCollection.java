package com.cardshifter.gdx;

import com.cardshifter.gdx.api.messages.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FieldsCollection<T> {

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
			fields.add(field);
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

	private void deserialize(Field field, Message message, DataInputStream data) throws IllegalArgumentException, IllegalAccessException, IOException {
		field.setAccessible(true);
		Class<?> type = field.getType();
		if (type == int.class) {
			field.setInt(message, data.readInt());
		}
		else if (type == String.class) {
			int length = data.readInt();
			StringBuilder str = new StringBuilder(length);
			for (int i = 0; i < length; i++) {
				str.append(data.readChar());
			}
			field.set(message, str.toString());
		}
//		else if (Enum.class.isAssignableFrom(type)) {
//		}
		else {
			throw new IOException("unknown type " + type);
		}
	}

	private void serialize(Field field, T obj, DataOutputStream out) throws IOException, IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		Class<?> type = field.getType();
		Object value = field.get(obj);
		if (type == int.class) {
			out.writeInt(field.getInt(obj));
		}
		else if (type == String.class) {
			String str = (String) value;
			out.writeInt(str.length());
			out.writeChars(str);
		}
//		else if (Enum.class.isAssignableFrom(type)) {
//		}
		else {
			throw new IOException("unknown type " + type);
		}
	}

	public FieldsCollection<T> orderByName() {
		List<Field> myFields = new ArrayList<Field>(fields);
		myFields.sort(new Comparator<Field>() {
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
