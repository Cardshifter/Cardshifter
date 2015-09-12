package com.cardshifter.server.utils.export;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.cardshifter.modapi.attributes.ECSAttributeData;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.zomis.cardshifter.ecs.effects.EffectComponent;

public class EntitySerializer extends JsonSerializer<Entity> {

	@Override
	public void serialize(Entity value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		
		ECSResourceMap map = value.getComponent(ECSResourceMap.class);
        if (map != null) {
            List<ECSResourceData> ress = map.getResources().collect(Collectors.toList());
            for (ECSResourceData res : ress) {
                jgen.writeObjectField(res.getResource().toString(), res.get());
            }
        }

        ECSAttributeMap attributes = value.getComponent(ECSAttributeMap.class);
        if (attributes != null) {
            List<ECSAttributeData> attribs = attributes.getAttributes().collect(Collectors.toList());
            for (ECSAttributeData attr : attribs) {
                jgen.writeObjectField(attr.getAttribute().toString(), attr.get());
            }
        }

        EffectComponent effect = value.getComponent(EffectComponent.class);
        if (effect != null) {
            jgen.writeObjectField("description", effect.getDescription());
        }

        ComponentRetriever<CreatureTypeComponent> creatureType = Retrievers.component(CreatureTypeComponent.class);
		if (creatureType.has(value)) {
			jgen.writeObjectField("type", creatureType.get(value).getAllTypes());
		}
		
		jgen.writeEndObject();		
	}

}
