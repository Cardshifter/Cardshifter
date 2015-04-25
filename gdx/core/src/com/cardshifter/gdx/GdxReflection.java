package com.cardshifter.gdx;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.serial.ReflField;
import com.cardshifter.api.serial.ReflectionInterface;

import java.util.*;

/**
 * Created by Simon on 4/25/2015.
 */
public class GdxReflection implements ReflectionInterface {

    private final Set<String> fieldNameSkip = new HashSet<String>(
            Arrays.asList("___clazz", "castableTypeMap", "expando", "typeMarker")
    );

    @Override
    public Object create(Class<?> aClass) throws Exception {
        return ClassReflection.newInstance(aClass);
    }

    @Override
    public ReflField[] getFields(Class<?> aClass) {
        Field[] fields = ClassReflection.getDeclaredFields(aClass);
        List<ReflField> result = new ArrayList<ReflField>(fields.length);
        for (Field field : fields) {
            String name = field.getName();
            if (fieldNameSkip.contains(name)) {
                continue;
            }
            result.add(new GdxField(field));
        }
        return result.toArray(new ReflField[result.size()]);
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
