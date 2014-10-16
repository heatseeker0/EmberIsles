package us.embercraft.emberisles.datatypes;

import java.io.Serializable;
import java.util.UUID;

public class Helper implements Serializable {
	public Helper(final WorldType type, final IslandLookupKey islandKey, final UUID playerId, final long expireTimestamp) {
		this.type = type;
		this.islandKey = islandKey;
		this.playerId = playerId;
		this.expireTimestamp = expireTimestamp;
	}
	
	public IslandLookupKey getIslandKey() {
		return islandKey;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	/**
	 * Returns the timestamp when this helper will be removed from the island.
	 * @return
	 */
	public long getExpireTimestamp() {
		return expireTimestamp;
	}
	
	public WorldType getWorldType() {
		return type;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Helper))
			return false;
		return ((Helper) o).playerId.equals(playerId) &&
				((Helper) o).islandKey.equals(islandKey) &&
				((Helper) o).type == type;
	}
	
	@Override
	public int hashCode() {
		if (hashCode == 0)
			hashCode = (31 + playerId.hashCode()) * 31  + islandKey.hashCode();
		return hashCode;
	}
	
	private transient int hashCode = 0;
	
	private final UUID playerId;
	private final IslandLookupKey islandKey;
	private final WorldType type;
	private final long expireTimestamp;
	
	private static final long serialVersionUID = 1L;
}
