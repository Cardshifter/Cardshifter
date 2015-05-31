package com.cardshifter.modapi.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.CopyableComponent;
import com.cardshifter.modapi.base.Entity;

public class ECSAttributeMap extends Component implements CopyableComponent {

	private final Map<ECSAttribute, ECSAttributeData> map = new HashMap<>();

	private ECSAttributeMap() {
	}

	public ECSAttributeMap set(ECSAttribute attr, String value) {
		ECSAttributeData data = getAttribute(attr);
		data.set(value);
		return this;
	}

    public Optional<ECSAttributeData> get(ECSAttribute res) {
        return Optional.ofNullable(map.get(res));
    }

    public ECSAttributeData getAttribute(ECSAttribute attr) {
		map.computeIfAbsent(attr, r -> new ECSAttributeData(getEntity(), r));
		return map.get(attr);
	}

	public static ECSAttributeMap createFor(Entity entity) {
		ECSAttributeMap attr = new ECSAttributeMap();
		entity.addComponent(attr);
		return attr;
	}
	
	public static ECSAttributeMap createOrGetFor(Entity entity) {
		ECSAttributeMap attr = entity.getComponent(ECSAttributeMap.class);
		if (attr == null) {
			attr = new ECSAttributeMap();
			entity.addComponent(attr);
		}
		return attr;
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public Stream<ECSAttributeData> getAttributes() {
		return map.values().stream();
	}

	@Override
	public Component copy(Entity copyTo) {
		ECSAttributeMap copy = new ECSAttributeMap();
		for (ECSAttributeData data : map.values()) {
			ECSAttributeData copyData = data.copy(copyTo);
			copy.map.put(data.getAttribute(), copyData);
		}
		return copy;
	}
	
}
