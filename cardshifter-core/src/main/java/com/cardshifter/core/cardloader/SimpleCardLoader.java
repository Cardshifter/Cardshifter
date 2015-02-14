package com.cardshifter.core.cardloader;

import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class SimpleCardLoader implements CardLoader<Path> {
    private static final Pattern property = Pattern.compile("^(\\w+)=(.+)$");
    private static final Pattern apply = Pattern.compile("^apply\\:(\\w+)\\:?(.+)?$");

    private static final Logger logger = LogManager.getLogger(SimpleCardLoader.class);

    @Override
    public Collection<Entity> loadCards(Path input, ECSGame game, ECSMod mod, ECSResource[] resources, ECSAttribute[] attributes) throws CardLoadingException {
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(input.toFile()), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            throw new CardLoadingException("Cannot find file " + input, e);
        }

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            List<Entity> entities = new ArrayList<>();
            String line;
            Entity entity = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[")) {
                    entity = game.newEntity();
                    logger.info("New entity: " + entity);
                    entities.add(entity);
                }
                else {
                    Matcher applyMatcher = apply.matcher(line);
                    if (applyMatcher.find()) {
                        logger.info("Applying " + line);
                        String methodName = applyMatcher.group(1);
                        String parameters = applyMatcher.group(2);
                        applyApply(entity, mod, methodName, parameters);
                    }

                    Matcher propertyMatcher = property.matcher(line);
                    if (propertyMatcher.find()) {
                        logger.info("Property " + line);
                        String propertyName = propertyMatcher.group(1);
                        String propertyValue = propertyMatcher.group(2);
                        applyProperty(entity, resources, attributes, propertyName, propertyValue);
                    }
                }

            }
            return entities;
        } catch (IOException e) {
            throw new CardLoadingException(e);
        }
    }

    private void applyApply(Entity entity, ECSMod mod, String methodName, String parameters) throws CardLoadingException {
        if (parameters == null) {
            Consumer<Entity> field = findConsumerField(mod, methodName);
            field.accept(entity);
        }
        else {
            Consumer<Entity> consumer = findConsumerMethod(mod, methodName, parameters);
            consumer.accept(entity);
        }
    }

    private Consumer<Entity> findConsumerMethod(ECSMod mod, String methodName, String parameters) throws CardLoadingException {
        Class<?> clazz = mod.getClass();
        List<Method> result = new ArrayList<>();
        do {
            result.addAll(Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList()));
            clazz = clazz.getSuperclass();
        }
        while (clazz != null);

        if (result.size() != 1) {
            throw new CardLoadingException("Not a unique method: " + methodName + " in " + mod + " found " + result.size() + " results.");
        }

        Method method = result.get(0);
        List<Object> callParams = transformParameters(parameters, method.getParameterTypes());

        try {
            method.setAccessible(true);
            return (Consumer<Entity>) method.invoke(mod, callParams.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CardLoadingException(e);
        }
    }

    private List<Object> transformParameters(String parameters, Class<?>[] parameterTypes) {
        List<Object> result = new ArrayList<>();
        while (parameters.length() > 0) {
            if (parameters.startsWith("\"")) {
                parameters = parameters.substring(1);
                int endIndex = parameters.indexOf('"');
                result.add(parameters.substring(0, endIndex));
                parameters = parameters.substring(endIndex + 1);
            }
            else {
                int nextComma = parameters.indexOf(',');
                if (nextComma == -1) {
                    nextComma = parameters.length();
                }
                result.add(Integer.parseInt(parameters.substring(0, nextComma)));
            }
            int nextComma = parameters.indexOf(',');
            if (nextComma != -1) {
                parameters = parameters.substring(nextComma + 1);
            }
            else {
                break;
            }
        }
        return result;
    }

    private Consumer<Entity> findConsumerField(ECSMod mod, String methodName) throws CardLoadingException {
        Class<?> clazz = mod.getClass();
        Optional<Field> result;
        do {
            result = Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getName().equals(methodName)).findAny();
            clazz = clazz.getSuperclass();
        }
        while (!result.isPresent() && clazz != null);

        if (result.isPresent()) {
            try {
                Field field = result.get();
                field.setAccessible(true);
                return (Consumer<Entity>) field.get(mod);
            } catch (IllegalAccessException e) {
                throw new CardLoadingException(e);
            }
        }
        throw new CardLoadingException("Field not found: " + methodName + " in " + mod);
    }

    private void applyProperty(Entity entity, ECSResource[] resources, ECSAttribute[] attributes, String propertyName, String propertyValue) throws CardLoadingException {
        final String cleanedName = CardLoaderHelper.sanitizeTag(propertyName);

        Optional<ECSResource> matchingResource = Arrays.stream(resources)
                .filter(res -> CardLoaderHelper.sanitizeTag(res.toString()).equals(cleanedName)).findAny();
        Optional<ECSAttribute> matchingAttribute = Arrays.stream(attributes)
                .filter(res -> CardLoaderHelper.sanitizeTag(res.toString()).equals(cleanedName)).findAny();

        if (matchingAttribute.isPresent()) {
            ECSAttributeMap resMap = ECSAttributeMap.createOrGetFor(entity);
            resMap.set(matchingAttribute.get(), propertyValue);
        }
        if (matchingResource.isPresent()) {
            ECSResourceMap resMap = ECSResourceMap.createOrGetFor(entity);
            try {
                resMap.set(matchingResource.get(), Integer.parseInt(propertyValue));
            }
            catch (NumberFormatException ex) {
                throw new CardLoadingException("Cannot set resource " + propertyName + " to " + propertyValue, ex);
            }
        }

        if (!matchingAttribute.isPresent() && !matchingResource.isPresent()) {
            throw new CardLoadingException("Cannot set " + propertyName + " to " + propertyValue + ": No such attribute or property");
        }
    }
}
