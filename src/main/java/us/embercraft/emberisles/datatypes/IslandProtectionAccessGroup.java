package us.embercraft.emberisles.datatypes;

public enum IslandProtectionAccessGroup {
    MEMBERS("members"),
    HELPERS("helpers"),
    PUBLIC("public");

    private IslandProtectionAccessGroup(final String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    /**
     * Returns the enum associated with the given access group
     * 
     * @param group
     * @return
     */
    public static IslandProtectionAccessGroup getEnum(String group) {
        if (group == null)
            throw new IllegalArgumentException("group cannot be null");
        group = group.toLowerCase();
        for (IslandProtectionAccessGroup type : IslandProtectionAccessGroup.values()) {
            if (type.getConfigKey().equals(group)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid group value " + group);
    }

    private final String configKey;
}
