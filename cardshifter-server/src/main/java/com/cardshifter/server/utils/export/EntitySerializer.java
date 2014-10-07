package com.cardshifter.server.utils.export;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.CreatureTypeComponent;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.Retrievers;
import net.zomis.cardshifter.ecs.resources.ECSResourceData;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EntitySerializer extends JsonSerializer<Entity> {

	@Override
	public void serialize(Entity value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		
		ECSResourceMap map = value.getComponent(ECSResourceMap.class);
		List<ECSResourceData> ress = map.getResources().collect(Collectors.toList());
		for (ECSResourceData res : ress) {
			jgen.writeObjectField(res.getResource().toString(), res.get());
		}
		
		ComponentRetriever<CreatureTypeComponent> creatureType = Retrievers.component(CreatureTypeComponent.class);
		if (creatureType.has(value)) {
			jgen.writeObjectField("type", creatureType.get(value).getCreatureType());
		}
		
		jgen.writeEndObject();		
	}

}
