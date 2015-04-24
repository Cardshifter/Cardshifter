package com.cardshifter.core.messages;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.messages.MessageTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Created by Simon on 4/24/2015.
 */
public class MessageTypeResolver implements TypeIdResolver {

    private JavaType mBaseType;

    public static Class<?> typeFor(String id) {
        return MessageTypeIdResolver.get(id);
    }

    @Override
    public void init(JavaType baseType) {
        mBaseType = baseType;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public String idFromValue(Object obj) {
        return idFromValueAndType(obj, obj.getClass());
    }

    @Override
    public String idFromBaseType() {
        throw new AssertionError("this should never happen");
    }

    @Override
    public String idFromValueAndType(Object obj, Class<?> clazz) {
        Message mess = (Message) obj;
        return mess.getCommand();
    }

    @Override
    public JavaType typeFromId(String type) {
        Class<?> clazz = MessageTypeIdResolver.get(type);
        if (clazz == null) {
            throw new UnsupportedOperationException("No such defined type: " + type);
        }
        return TypeFactory.defaultInstance().constructSpecializedType(mBaseType, clazz);
    }


}
