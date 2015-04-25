package com.cardshifter.api.serial;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import com.cardshifter.api.LogInterface;

public class FieldsCollection<T> {

	private final List<ReflField> fields;
    private final LogInterface logger;
    private final ReflectionInterface refl;

    public FieldsCollection(List<ReflField> fields, LogInterface logger, ReflectionInterface refl) {
		this.fields = Collections.unmodifiableList(fields);
        this.logger = logger;
        this.refl = refl;
	}

	public static <T> FieldsCollection<T> gather(T object, LogInterface logger, ReflectionInterface refl) {
		List<ReflField> fields = new ArrayList<ReflField>();
		Class<?> clazz = object.getClass();
		while (clazz != null) {
			addFields(refl, fields, clazz);
			clazz = clazz.getSuperclass();
		}
		return new FieldsCollection<T>(fields, logger, refl);
	}

	private static void addFields(ReflectionInterface refl, List<ReflField> fields, Class<?> clazz) {
		for (ReflField field : refl.getFields(clazz)) {
			if (!field.isStatic()) {
				fields.add(field);
			}
		}
	}
	
	public byte[] serialize(T message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		try {
			for (ReflField field : fields) {
				serialize(field, message, out);
			}
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException(e.toString());
		}
		byte[] data = baos.toByteArray();
		
		baos = new ByteArrayOutputStream();
		out = new DataOutputStream(baos);
		out.writeInt(data.length);
		baos.write(data);
		return baos.toByteArray();
	}

	private Object deserialize(Class<?> type, DataInputStream data, ReflField field) throws IOException {
		if (type == int.class || type == Integer.class) {
			int value = data.readInt();
			return value;
		}
		else if (type == String[].class) {
			int count = data.readInt();
			String[] str = new String[count];
			for (int i = 0; i < str.length; i++) {
				str[i] = (String) deserialize(String.class, data, null);
			}
			return str;
		}
		else if (type == int[].class) {
			int count = data.readInt();
			int[] array = new int[count];
			for (int i = 0; i < array.length; i++) {
				array[i] = (Integer) deserialize(Integer.class, data, null);
			}
			return array;
		}
		else if (type == Boolean.class) {
			byte boolValue = data.readByte();
			Boolean bool = null;
			if (boolValue != 2) {
				bool = (boolValue == 1);
			}
			return bool;
		}
		else if (type == boolean.class) {
			byte boolValue = data.readByte();
			return boolValue == 1;
		}
		else if (type == String.class) {
			int length = data.readInt();
			StringBuilder str = new StringBuilder(length);
			for (int i = 0; i < length; i++) {
				str.append(data.readChar());
			}
			return str.toString();
		}
		else if (refl.isEnum(type)) {
			Object[] values = type.getEnumConstants();
			int ordinal = data.readInt();
			return values[ordinal];
		}
		else if (type == Map.class) {
			if (field == null) {
				throw new NullPointerException("Field cannot be null when deserializing Map");
			}

            Class<?> keyClass = field.getGenericType(0);
            Class<?> valueClass = field.getGenericType(1);

			Map<Object, Object> map = new HashMap<Object, Object>();
			int size = data.readInt();
			for (int i = 0; i < size; i++) {
				Object key = deserialize(keyClass, data, null);
				Object value = deserialize(valueClass, data, null);
				map.put(key, value);
			}
			return map;
		}
		else if (type == Object.class) {
			String clazzName = (String) deserialize(String.class, data, null);
			try {
				Class<?> clazz = refl.forName(clazzName);
				Object obj = deserialize(clazz, data, field);
//				logger.debug("Deserialized object: " + obj);
				return obj;
			} catch (Exception e) {
				throw new IOException(e.toString());
			}
		}
		else {
			logger.info("Using recursive deserialization for " + type);
			try {
                Object obj = refl.create(type);
				FieldsCollection<Object> fields = FieldsCollection.gather(obj, logger, refl);
				fields = fields.orderByName();
				data.readInt(); // length of upcoming data, ignored on recursive deserialization
				fields.read(obj, data);
				return obj;
			} catch (Exception e) {
				throw new IOException(e.toString());
			}
		}
	}
	
	private void deserialize(ReflField field, Object message, DataInputStream data) throws Exception {
//		logger.debug("read field " + field + " for " + message);
		field.setAccessible(true);
		Class<?> type = field.getType();
		field.set(message, deserialize(type, data, field));
	}

	private void serialize(Class<?> type, Object value, DataOutputStream out, ReflField field)
			throws IOException, IllegalArgumentException, IllegalAccessException {
//		logger.info("Serializing " + type + ": " + value);
		if (type == int.class || type == Integer.class) {
			out.writeInt((Integer) value);
		}
		else if (type == String.class) {
			String str = (String) value;
			out.writeInt(str.length());
			out.writeChars(str);
		}
		else if (type == Boolean.class) {
			Boolean bool = (Boolean) value;
			int boolValue = bool == null ? 2 : bool ? 1 : 0;
			out.writeByte(boolValue);
		}
		else if (type == boolean.class) {
			boolean bool = (Boolean) value;
			int boolValue = bool ? 1 : 0;
			out.writeByte(boolValue);
		}
		else if (type == String[].class) {
			String[] arr = (String[]) value;
			out.writeInt(arr.length);
			for (int i = 0; i < arr.length; i++) {
				serialize(String.class, arr[i], out, null);
			}
		}
		else if (type == int[].class) {
			int[] array = (int[]) value;
			out.writeInt(array.length);
			for (int i = 0; i < array.length; i++) {
				serialize(int.class, array[i], out, null);
			}
		}
		else if (refl.isEnum(type)) {
			Enum<?> enumValue = (Enum<?>) value;
			out.writeInt(enumValue.ordinal());
		}
		else if (type == Map.class) {
			if (field == null) {
				throw new NullPointerException("Field cannot be null when serializing Map");
			}

			Class<?> keyClass = field.getGenericType(0);
			Class<?> valueClass = field.getGenericType(1);

			Map<Object, Object> map = (Map<Object, Object>) value;
			out.writeInt(map.size());
			for (Map.Entry<Object, Object> ee : map.entrySet()) {
				serialize(keyClass, ee.getKey(), out, null);
				serialize(valueClass, ee.getValue(), out, null);
			}
		}
		else if (type == Object.class) {
			String clazzName = (String) value.getClass().getName();
			serialize(String.class, clazzName, out, null);
			serialize(value.getClass(), value, out, null);
		}
		else {
			logger.info("Using recursive serialization for " + type);
			FieldsCollection<Object> fields = FieldsCollection.gather(value, logger, refl);
			fields = fields.orderByName();
			byte[] b = fields.serialize(value);
			out.write(b);
		}
	}
	
	private void serialize(ReflField field, T obj, DataOutputStream out) throws Exception {
		field.setAccessible(true);
		Class<?> type = field.getType();
		Object value = field.get(obj);
		serialize(type, value, out, field);
	}

	public FieldsCollection<T> orderByName() {
		List<ReflField> myFields = new ArrayList<ReflField>(fields);
		Collections.sort(myFields, new Comparator<ReflField>() {
			@Override
			public int compare(ReflField o1, ReflField o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return new FieldsCollection<T>(myFields, logger, refl);
	}

	public FieldsCollection<T> putFirst(String fieldName) {
		List<ReflField> myFields = new ArrayList<ReflField>(fields);
		for (ReflField field : myFields) {
			if (field.getName().equals(fieldName)) {
				myFields.remove(field);
				myFields.add(0, field);
				return new FieldsCollection<T>(myFields, logger, refl);
			}
		}
		throw new IllegalArgumentException("Field name not found: " + fieldName);
	}

	public void read(Object message, DataInputStream data) throws IOException {
		try {
			for (ReflField field : fields) {
				deserialize(field, message, data);
			}
		}
		catch (Exception ex) {
			throw new IOException(ex.toString());
		}
	}

	public FieldsCollection<T> skipFirst() {
		List<ReflField> myFields = new ArrayList<ReflField>(fields);
		myFields.remove(0);
		return new FieldsCollection<T>(myFields, logger, refl);
	}

}
