package net.zomis.cardshifter.ecs.usage;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.modapi.base.Component;

public class ConfigComponent extends Component {
	
	private final Map<String, Object> configs = new HashMap<>();
	private boolean configured;

	public ConfigComponent addConfig(String key, Object config) {
		configs.put(key, config);
		return this;
	}
	
	public Map<String, Object> getConfigs() {
		return new HashMap<>(configs);
	}
	
	public boolean isConfigured() {
		return configured;
	}
	
	public void setConfigured(boolean configured) {
		this.configured = configured;
	}

}
