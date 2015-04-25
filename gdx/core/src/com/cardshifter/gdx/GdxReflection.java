package com.cardshifter.gdx;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.cardshifter.api.serial.ReflField;
import com.cardshifter.api.serial.ReflectionInterface;

/**
 * Created by Simon on 4/25/2015.
 */
public class GdxReflection implements ReflectionInterface {

    @Override
    public Object create(Class<?> aClass) throws Exception {
        return ClassReflection.newInstance(aClass);
    }

    @Override
    public ReflField[] getFields(Class<?> aClass) {
        Field[] fields = ClassReflection.getDeclaredFields(aClass);
        ReflField[] result = new ReflField[fields.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new GdxField(fields[i]);
        }
        return result;
    }

    @Override
    public boolean isEnum(Class<?> aClass) {
        return ClassReflection.isAssignableFrom(Enum.class, aClass);
    }

    @Override
    public Class<?> forName(String s) throws Exception {
        return ClassReflection.forName(s);
    }


}
