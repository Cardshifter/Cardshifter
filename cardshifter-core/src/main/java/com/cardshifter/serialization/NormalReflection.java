package com.cardshifter.serialization;

import com.cardshifter.api.serial.ReflectionInterface;
import com.cardshifter.api.serial.ReflField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Created by Simon on 4/25/2015.
 */
public class NormalReflection implements ReflectionInterface {

    @Override
    public Object create(Class<?> type) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    @Override
    public ReflField[] getFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        ReflField[] result = new ReflField[fields.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new JavaReflectField(fields[i]);
        }
        return result;
    }

    @Override
    public boolean isEnum(Class<?> type) {
        return Enum.class.isAssignableFrom(type);
    }

    @Override
    public Class<?> forName(String clazzName) throws Exception {
        return Class.forName(clazzName);
    }
}
