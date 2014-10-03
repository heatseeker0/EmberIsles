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
	
	private final String configKey;
}
