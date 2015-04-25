package com.cardshifter.serialization;

import com.cardshifter.api.serial.ReflField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Simon on 4/25/2015.
 */
public class JavaReflectField implements ReflField {

    private final Field field;

    public JavaReflectField(Field field) {
        this.field = field;
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    @Override
    public Class<?> getGenericType(int i) {
        Type genericFieldType = field.getGenericType();
        if (!(genericFieldType instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Cannot deserialize a Map without generics types");
        }
        ParameterizedType aType = (ParameterizedType) genericFieldType;
        Type[] fieldArgTypes = aType.getActualTypeArguments();
        return (Class<?>) fieldArgTypes[i];
    }

    @Override
    public void setAccessible(boolean b) {
        field.setAccessible(true);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public void set(Object obj, Object value) throws Exception {
        field.set(obj, value);
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Object get(Object obj) throws Exception {
        return field.get(obj);
    }
}
