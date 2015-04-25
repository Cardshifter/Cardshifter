package com.cardshifter.api.serial;

/**
 * Created by Simon on 4/25/2015.
 */
public interface ReflField {

    boolean isStatic();

    Class<?> getGenericType(int i);

    void setAccessible(boolean b);

    Class<?> getType();

    void set(Object message, Object value) throws Exception;

    String getName();

    Object get(Object obj) throws Exception;

}
