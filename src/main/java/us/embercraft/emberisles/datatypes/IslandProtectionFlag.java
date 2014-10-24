package us.embercraft.emberisles.datatypes;

public enum IslandProtectionFlag {
    FRIENDLY_MOBS(0, "friendly-mobs"),
    HOSTILE_MOBS(1, "hostile-mobs"),
    INTERACT_DOORS(2, "interact-doors"),
    INTERACT_SWITCHES(3, "interact-switches"),
    OPEN_CONTAINERS(4, "open-containers"),
    INTERACT_ANVILS(5, "interact-anvils"),
    RIDE(6, "ride"),
    PICK_GROUND_ITEMS(7, "pick-ground-items");

    private IslandProtectionFlag(final int id, final String configKey) {
        this.id = id;
        this.configKey = configKey;
    }

    public int id() {
        return this.id;
    }

    public String getConfigKey() {
        return this.configKey;
    }

    /**
     * Returns the enum associated with the given flag
     * 
     * @param flag
     * @return
     */
    public static IslandProtectionFlag getEnum(String flag) {
        if (flag == null)
            throw new IllegalArgumentException("flag cannot be null");
        flag = flag.toLowerCase();
        for (IslandProtectionFlag type : IslandProtectionFlag.values()) {
            if (type.getConfigKey().equals(flag)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid flag value " + flag);
    }

    private final int id;
    private final String configKey;
}
