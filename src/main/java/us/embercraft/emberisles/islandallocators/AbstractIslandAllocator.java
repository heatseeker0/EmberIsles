package us.embercraft.emberisles.islandallocators;

import java.util.Collection;

import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandLookupKey;

public abstract class AbstractIslandAllocator {
	/**
	 * Updates allocator internal data structures with the provided occupied islands. The allocator will mark these
	 * islands as occupied and will start issuing new islands after the last provided island.
	 * 
	 * <p>It is recommended to {@link #setLineLength(int)} to the final value before calling this function.</p>
	 * 
	 * @param islands Islands to add
	 */
	public abstract void updateCurrentGridOccupied(Collection<Island> islands);
	
	/**
	 * Updates allocator internal data structures with the provided free islands. The allocator will mark these
	 * island spots as reserved and will start issuing new islands after the last provided island.
	 * 
	 * <p>It is recommended to {@link #setLineLength(int)} to the final value before calling this function.</p>
	 * 
	 * @param islands Islands to add
	 */
	public abstract void updateCurrentGridFree(Collection<IslandLookupKey> keys);
	
	/**
	 * Returns the next available free island according to this allocator
	 * policies and settings.
	 */
	public abstract IslandLookupKey next();
}
