package us.embercraft.emberisles.datatypes;

public enum IslandProtectionAccessLevel {
	MEMBERS("members"),
	HELPERS("helpers"),
	PUBLIC("public");
	
	private IslandProtectionAccessLevel(final String configKey) {
		this.configKey = configKey;
	}
	
	public String getConfigKey() {
		return configKey;
	}
	
	private final String configKey;
}
