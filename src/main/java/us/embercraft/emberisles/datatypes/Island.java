package us.embercraft.emberisles.datatypes;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

import us.embercraft.emberisles.EmberIsles;

public class Island implements Serializable {

    public Island(final int gridX, final int gridZ) {
        this.islandGridX = gridX;
        this.islandGridZ = gridZ;
        this.createTime = System.currentTimeMillis();
        initDefaultIslandData();
    }

    public Island(final IslandLookupKey key) {
        this(key.getGridX(), key.getGridZ());
    }

    /**
     * Initializes and sets common island data to default values.
     */
    private void initDefaultIslandData() {
        for (IslandProtectionAccessLevel accessLevel : IslandProtectionAccessLevel.values()) {
            protectionFlags.put(accessLevel, (BitSet) EmberIsles.getInstance().getDefaultProtectionFlags(accessLevel).clone());
        }
    }

    /**
     * Sets the private island spawn to the specified non-null location.
     * 
     * @param loc
     */
    public void setSpawn(Location loc) {
        if (loc != null) {
            spawn = new ImmutableSimpleLocation(loc);
        }
    }

    /**
     * Gets the private island spawn, or null if one hasn't been set.
     * 
     * <p>
     * In practice, island spawn is set in the island schematic by placing a special block (e.g. bedrock or portal frame) so it'll seldom be null.
     * </p>
     * 
     * @return The private island spawn, or null if one hasn't been set
     */
    public ImmutableSimpleLocation getSpawn() {
        return spawn;
    }

    /**
     * Sets the public island warp to the specified non-null location.
     * 
     * @param loc
     */
    public void setWarp(Location loc) {
        if (loc != null) {
            warp = new ImmutableSimpleLocation(loc);
        }
    }

    /**
     * Gets the public island warp, or null if one hasn't been set.
     * 
     * @return The public island warp, or null if one hasn't been set.
     */
    public ImmutableSimpleLocation getWarp() {
        return warp;
    }

    public boolean isWarpEnabled() {
        return warp != null && warpEnabled;
    }

    public void enableWarp() {
        if (warp != null) {
            warpEnabled = true;
        }
    }

    public void disableWarp() {
        warpEnabled = false;
    }

    /**
     * Returns the status of the island lock. See {@link #lockIsland()} and {@link #unlockIsland()}.
     * 
     * @return True if the island is locked
     */
    public boolean isIslandLocked() {
        return islandLocked;
    }

    /**
     * Lock the island preventing anyone except the island owner, members and
     * helpers from entering the island space by any means.
     */
    public void lockIsland() {
        islandLocked = true;
    }

    /**
     * Unlocks the island allowing anyone (including non-members) to enter the
     * island space.
     */
    public void unlockIsland() {
        islandLocked = false;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setOwnerLoginTime(final long time) {
        ownerLastLoginTime = time;
    }

    public long getOwnerLoginTime() {
        return ownerLastLoginTime;
    }

    public void setSchematic(final String schematic) {
        this.schematic = schematic;
    }

    public String getSchematic() {
        return schematic;
    }

    public int getIslandGridX() {
        return islandGridX;
    }

    public int getIslandGridZ() {
        return islandGridZ;
    }

    public boolean getProtectionFlag(final IslandProtectionAccessLevel level, final IslandProtectionFlag flag) {
        return protectionFlags.get(level).get(flag.id());
    }

    public void setProtectionFlag(final IslandProtectionAccessLevel level, final IslandProtectionFlag flag, final boolean value) {
        protectionFlags.get(level).set(flag.id(), value);
    }

    public boolean toggleProtectionFlag(final IslandProtectionAccessLevel level, final IslandProtectionFlag flag) {
        protectionFlags.get(level).flip(flag.id());
        return getProtectionFlag(level, flag);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean removeMember(UUID player) {
        return members.remove(player);
    }

    public boolean addMember(UUID player) {
        return members.add(player);
    }

    public boolean isBanned(UUID player) {
        return bannedPlayers.contains(player);
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public boolean banPlayer(UUID player) {
        return bannedPlayers.remove(player);
    }

    public boolean unbanPlayer(UUID player) {
        return bannedPlayers.add(player);
    }

    public IslandLookupKey getLookupKey() {
        if (lookupKey == null) {
            lookupKey = new IslandLookupKey(this);
        }
        return lookupKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Island))
            return false;
        return ((Island) o).islandGridX == islandGridX && ((Island) o).islandGridZ == islandGridZ;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0)
            hashCode = (31 + islandGridX) * 31 + islandGridZ;
        return hashCode;
    }

    @Override
    public String toString() {
        return String.format("[owner: %s, gridX: %s, gridZ: %s, schematic: %s]", EmberIsles.getInstance().getPlayerManager().getPlayerName(owner), islandGridX, islandGridZ);
    }

    public Location getCornerA() {
        return cornerA;
    }

    public void setCornerA(Location cornerA) {
        this.cornerA = cornerA;
    }

    public Location getCornerB() {
        return cornerB;
    }

    public void setCornerB(Location cornerB) {
        this.cornerB = cornerB;
    }

    private transient int hashCode = 0;
    private transient IslandLookupKey lookupKey = null;
    private transient Location cornerA = null;
    private transient Location cornerB = null;

    private final int islandGridX;
    private final int islandGridZ;
    private ImmutableSimpleLocation spawn;
    private ImmutableSimpleLocation warp;
    private boolean warpEnabled = false;
    private boolean islandLocked = false;
    private final long createTime;
    private String schematic;
    private long ownerLastLoginTime;
    private final Map<IslandProtectionAccessLevel, BitSet> protectionFlags = new HashMap<>();
    private UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();

    private static final long serialVersionUID = 1L;
}
