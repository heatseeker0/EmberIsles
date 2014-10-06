package us.embercraft.emberisles.datatypes;

public enum WorldType {
	NORMAL_WORLD("normal"),
	CHALLENGE_WORLD("challenge"),
	HARDCORE_WORLD("hardcore");
	
	private WorldType(final String configKey) {
		this.configKey = configKey;
	}
	
	public String getConfigKey() {
		return configKey;
	}
	
	/**
	 * Returns the enum associated with the given config key
	 * @param configKey
	 * @return
	 */
	public static WorldType getEnum(String configKey) {
		if (configKey == null)
			throw new IllegalArgumentException("configKey cannot be null");
		configKey = configKey.toLowerCase();
		for (WorldType type : WorldType.values()) {
			if (type.getConfigKey().equals(configKey)) {
				return type;
			}
		}
		return NORMAL_WORLD;
	}
	
	private final String configKey;
}
