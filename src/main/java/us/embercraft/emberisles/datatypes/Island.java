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
        for (IslandProtectionAccessGroup accessLevel : IslandProtectionAccessGroup.values()) {
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

    /**
     * Returns true if the island warp is enabled and set (not null).
     * 
     * @return
     */
    public boolean isWarpEnabled() {
        return warp != null && warpEnabled;
    }

    /**
     * Enables the island warp if one is already set (not null). Does nothing if the warp is null.
     */
    public void enableWarp() {
        if (warp != null) {
            warpEnabled = true;
        }
    }

    /**
     * Disables the island warp.
     */
    public void disableWarp() {
        warpEnabled = false;
    }

    /**
     * Returns the status of the island lock. See {@link #lock()} and {@link #unlock()}.
     * 
     * @return True if the island is locked
     */
    public boolean isLocked() {
        return islandLocked;
    }

    /**
     * Lock the island preventing anyone except the island owner, members and
     * helpers from entering the island space by any means.
     */
    public void lock() {
        islandLocked = true;
    }

    /**
     * Unlocks the island allowing anyone (including non-members) to enter the
     * island space.
     */
    public void unlock() {
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

    public boolean getProtectionFlag(final IslandProtectionAccessGroup level, final IslandProtectionFlag flag) {
        return protectionFlags.get(level).get(flag.id());
    }

    public void setProtectionFlag(final IslandProtectionAccessGroup level, final IslandProtectionFlag flag, final boolean value) {
        protectionFlags.get(level).set(flag.id(), value);
    }

    public boolean toggleProtectionFlag(final IslandProtectionAccessGroup level, final IslandProtectionFlag flag) {
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

    /**
     * Bans specified player.
     * 
     * @param player Player Id to ban
     * @return True if player was banned, false if it was already banned
     */
    public boolean banPlayer(UUID player) {
        return bannedPlayers.add(player);
    }

    /**
     * Unbans specified player.
     * 
     * @param player Player Id to unban
     * @return True if player was unbanned, false if player was already unbanned
     */
    public boolean unbanPlayer(UUID player) {
        return bannedPlayers.remove(player);
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

    /**
     * Gets the island corner A (upper left) in Bukkit world coordinates.
     * 
     * @return Island corner A (upper left) in Bukkit world coordinates.
     */
    public Location getCornerA() {
        return cornerA;
    }

    public void setCornerA(Location cornerA) {
        this.cornerA = cornerA;
    }

    /**
     * Gets the island corner B (bottom right) in Bukkit world coordinates.
     * 
     * @return Island corner B (bottom right) in Bukkit world coordinates.
     */
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
    private final Map<IslandProtectionAccessGroup, BitSet> protectionFlags = new HashMap<>();
    private UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();

    private static final long serialVersionUID = 1L;
}
