package net.zomis.cardshifter.ecs.config;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.config.PlayerConfig;
import com.cardshifter.modapi.base.Component;

public class ConfigComponent extends Component {
	
	private final Map<String, PlayerConfig> configs = new HashMap<>();
	private boolean configured;

	public ConfigComponent addConfig(String key, PlayerConfig config) {
		configs.put(key, config);
		return this;
	}
	
	public Map<String, PlayerConfig> getConfigs() {
		return new HashMap<>(configs);
	}
	
	public boolean isConfigured() {
		return configured;
	}
	
	public void setConfigured(boolean configured) {
		this.configured = configured;
	}
	
	public <T extends PlayerConfig> T getConfig(Class<T> configClass) {
		for (PlayerConfig confObject : configs.values()) {
			if (confObject.getClass() == configClass) {
				return configClass.cast(confObject);
			}
		}
		return null;
	}

}
