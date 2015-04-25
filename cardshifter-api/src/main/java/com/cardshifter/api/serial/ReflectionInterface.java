package com.cardshifter.api.serial;

/**
 * Created by Simon on 4/25/2015.
 */
public interface ReflectionInterface {


    Object create(Class<?> type) throws Exception;

    ReflField[] getFields(Class<?> clazz);

    boolean isEnum(Class<?> type);

    Class<?> forName(String clazzName) throws Exception;

}
