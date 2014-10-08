package us.embercraft.emberisles.datatypes;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Implements a fast tuple based on two integers that can be used as a
 * lookup key for a HashMap.
 * 
 * The hash code is simply the product of the two integers.
 * 
 * @author Catalin Ionescu <cionescu@gmail.com>
 *
 */
@Immutable
@ThreadSafe
public class IslandLookupKey {
	public IslandLookupKey(int islandGridX, int islandGridZ) {
		this.islandGridX = islandGridX;
		this.islandGridZ = islandGridZ;
		hashCode = (31 + islandGridX) * 31  + islandGridZ;
	}
	
	public IslandLookupKey(final Island island) {
		this.islandGridX = island.getIslandGridX();
		this.islandGridZ = island.getIslandGridZ();
		hashCode = (31 + islandGridX) * 31  + islandGridZ;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof IslandLookupKey))
			return false;
		return ((IslandLookupKey) o).islandGridX == islandGridX &&
				((IslandLookupKey) o).islandGridZ == islandGridZ;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	public int getGridX() {
		return islandGridX;
	}
	
	public int getGridZ() {
		return islandGridZ;
	}
	
	@Override
	public String toString() {
		return String.format("[x: %s, z: %s]", islandGridX, islandGridZ);
	}

	private final int islandGridX;
	private final int islandGridZ;
	private final int hashCode;
}
