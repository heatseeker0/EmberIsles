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
	
    /**
     * Initializes and sets common island data to default values.
     */
    private void initDefaultIslandData() {
    	for (IslandProtectionAccessLevel accessLevel : IslandProtectionAccessLevel.values()) {
    		protectionFlags.put(accessLevel, (BitSet) EmberIsles.getInstance().getDefaultProtectionFlags(accessLevel).clone());
    	}
    }
    
    /**
     * Sets the public island spawn (warp) to the specified non-null location.
     * @param loc
     */
    public void setSpawn(Location loc) {
    	if (loc != null) {
    		spawn = new ImmutableSimpleLocation(loc);
    	}
    }
    
    public ImmutableSimpleLocation getSpawn() {
    	return spawn;
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
    
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Island))
			return false;
		return ((Island) o).islandGridX == islandGridX &&
				((Island) o).islandGridZ == islandGridZ;
	}
	
	@Override
	public int hashCode() {
		if (hashCode == 0)
			hashCode = (31 + islandGridX) * 31  + islandGridZ;
		return hashCode;
	}
	
	@Override
	public String toString() {
		return String.format("[owner: %s, gridX: %s, gridZ: %s, schematic: %s]", EmberIsles.getInstance().getPlayerManager().getPlayerName(owner), islandGridX, islandGridZ);
	}
	
	private transient int hashCode = 0;
	
    private final int islandGridX;
    private final int islandGridZ;
    private ImmutableSimpleLocation spawn;
    private final long createTime;
    private String schematic;
    private long ownerLastLoginTime;
    private final Map<IslandProtectionAccessLevel, BitSet> protectionFlags = new HashMap<>();
    private UUID owner;
    private final Set<UUID> members = new HashSet<>();

	private static final long serialVersionUID = 1L;
}
