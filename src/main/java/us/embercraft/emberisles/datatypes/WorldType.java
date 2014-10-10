package us.embercraft.emberisles.datatypes;

public enum WorldType {
	NORMAL_WORLD("normal"),
	CHALLENGE_WORLD("challenge"),
	HARDCORE_WORLD("hardcore");
	
	private WorldType(final String configKey) {
		this.configKey = configKey;
	}
	
	/**
	 * Returns the configuration key for this world type. Can also be used to print the world type.
	 * @return Configuration key
	 */
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
		throw new IllegalArgumentException("Invalid configKey value " + configKey);
	}
	
	private final String configKey;
}
