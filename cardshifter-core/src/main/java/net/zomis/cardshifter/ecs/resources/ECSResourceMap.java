package net.zomis.cardshifter.ecs.resources;

import java.util.HashMap;
import java.util.Map;

import net.zomis.cardshifter.ecs.base.Component;

public class ECSResourceMap extends Component {

	private final Map<ECSResource, ECSResourceData> map = new HashMap<>();

	public ECSResourceMap() {
	}

	public ECSResourceMap set(ECSResource res, int value) {
		ECSResourceData data = getResource(res);
		data.set(value);
		return this;
	}

	public ECSResourceData getResource(ECSResource res) {
		map.computeIfAbsent(res, r -> new ECSResourceData(getEntity(), r));
		return map.get(res);
	}
	
	
	
	
}
