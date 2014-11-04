package com.cardshifter.modapi.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.CopyableComponent;
import com.cardshifter.modapi.base.Entity;

public class ECSAttributeMap extends Component implements CopyableComponent {

	private final Map<ECSAttribute, ECSAttributeData> map = new HashMap<>();

	private ECSAttributeMap() {
	}

	public ECSAttributeMap set(ECSAttribute res, String value) {
		ECSAttributeData data = getAttribute(res);
		data.set(value);
		return this;
	}

	public ECSAttributeData getAttribute(ECSAttribute res) {
		map.computeIfAbsent(res, r -> new ECSAttributeData(getEntity(), r));
		return map.get(res);
	}

	public static ECSAttributeMap createFor(Entity entity) {
		ECSAttributeMap res = new ECSAttributeMap();
		entity.addComponent(res);
		return res;
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
