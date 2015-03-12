package com.cardshifter.core.cardloader;

import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.zomis.cardshifter.ecs.effects.Effects;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cardshifter.core.cardloader.CardLoaderHelper.*;

/**
 * @author Frank van Heeswijk
 */
public class JsEffectsCardLoader implements CardLoader<Path> {
    @Override
    public Collection<Entity> loadCards(final Path path, final ECSGame game, final ECSMod mod, final ECSResource[] resources, final ECSAttribute[] attributes) throws CardLoadingException {
        try {
            List<Entity> entities = new ArrayList<>();

            List<ECSResource> resourcesList = (resources == null) ? Arrays.asList() : Arrays.asList(resources);
            List<ECSAttribute> attributesList = (attributes == null) ? Arrays.asList() : Arrays.asList(attributes);

            List<String> tags = Stream.concat(resourcesList.stream(), attributesList.stream())
                .map(ecsElement -> sanitizeTag(ecsElement.toString()))
                .collect(Collectors.toList());

            if (requiredTags().stream().anyMatch(tags::contains)) {
                throw new CardLoadingException("Tags " + requiredTags() + " are required by default, you cannot submit them in the resource or attribute fields.");
            }

            List<String> duplicateTags = tags.stream()
                .collect(Collectors.groupingBy(i -> i))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            if (!duplicateTags.isEmpty()) {
                throw new CardLoadingException("Tags " + duplicateTags + " have been input multiple times, this is not allowed.");
            }

            Map<String, ECSResource> ecsResourcesMap = resourcesList.stream()
                .collect(Collectors.toMap(ecsResource -> sanitizeTag(ecsResource.toString()), i -> i));

            Map<String, ECSAttribute> ecsAttributesMap = attributesList.stream()
                .collect(Collectors.toMap(ecsAttribute -> sanitizeTag(ecsAttribute.toString()), i -> i));

            ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
            Path jsPath = Paths.get(getClass().getResource("JsonEffectsCardLoader.js").toURI());
            scriptEngine.eval(new InputStreamReader(new FileInputStream(jsPath.toFile()), StandardCharsets.UTF_8));
            scriptEngine.eval(new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8));

            Invocable invocable = (Invocable)scriptEngine;
            invocable.invokeFunction("loadDSL", mod.dslClass().getName());
//            invocable.invokeFunction("loadDSLManual", mod.dslClass().getName());

            ScriptObjectMirror cards = (ScriptObjectMirror)invocable.invokeFunction("getCards");
            if (cards.isArray()) {
                int length = Integer.parseInt(cards.getMember("length").toString());
                for (int i = 0; i < length; i++) {
                    ScriptObjectMirror card = (ScriptObjectMirror)cards.getSlot(i);

                    Entity entity = game.newEntity();
                    entities.add(entity);

                    ECSResourceMap resourceMap = ECSResourceMap.createFor(entity);
                    ECSAttributeMap attributeMap = ECSAttributeMap.createFor(entity);

                    card.forEach((tag, value) -> {
                        String sanitizedTag = sanitizeTag(tag);
                        if (ecsResourcesMap.containsKey(sanitizedTag)) {
                            resourceMap.set(ecsResourcesMap.get(sanitizedTag), Integer.parseInt(value.toString()));
                        }
                        else if (ecsAttributesMap.containsKey(sanitizedTag)) {
                            attributeMap.set(ecsAttributesMap.get(sanitizedTag), value.toString());
                        }
                        else {
                            //special attribute
                            if (sanitizedTag.startsWith("on")) {
                                String eventClassName = tag.substring(2, tag.length()) + "Event";
                                Class<?> uncheckedEventClass = mod.getEventMapping().get(eventClassName);
                                if (uncheckedEventClass == null) {
                                    throw new UncheckedCardLoadingException("Event " + eventClassName + " has not been found");
                                }
                                if (!IEvent.class.isAssignableFrom(uncheckedEventClass)) {
                                    throw new UncheckedCardLoadingException("Event " + eventClassName + ": " + uncheckedEventClass + " does not implement IEvent");
                                }
                                Class<? extends IEvent> eventClass = IEvent.class.getClass().cast(uncheckedEventClass);

                                Effects effects = new Effects();
                                Function<Entity, ECSSystem> effect = effects.triggerSystem(
                                    eventClass,
                                    (innerEntity, event) -> true,
                                    (innerEntity, event) -> card.callMember(tag, invokeFunction(invocable, "makeJSGame", game))
                                );
                                game.addSystem(effect.apply(entity));
                            }
                        }
                    });
                }
            }

            return entities;
        } catch (IOException | ScriptException | URISyntaxException | NoSuchMethodException ex) {
            throw new CardLoadingException(ex);
        } catch (UncheckedCardLoadingException ex) {
            throw new CardLoadingException(ex.getMessage(), ex.getCause());
        }
    }

    private static Object invokeFunction(final Invocable invocable, final String name, final Object... args) {
        try {
            return invocable.invokeFunction(name, args);
        } catch (ScriptException | NoSuchMethodException ex) {
            throw new UncheckedCardLoadingException(ex);
        }
    }
}
