package com.cardshifter.gdx;

import com.badlogic.gdx.utils.reflect.Field;
import com.cardshifter.api.serial.ReflField;

/**
 * Created by Simon on 4/25/2015.
 */
public class GdxField implements ReflField {
    private final Field field;

    public GdxField(Field field) {
        this.field = field;
    }

    @Override
    public boolean isStatic() {
        return field.isStatic();
    }

    @Override
    public Class<?> getGenericType(int i) {
        return field.getElementType(i);
    }

    @Override
    public void setAccessible(boolean b) {
        field.setAccessible(b);
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
    public Object get(Object o) throws Exception {
        return field.get(o);
    }

    @Override
    public String toString() {
        return "Field:" + field.getName();
    }
}
