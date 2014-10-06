package us.embercraft.emberisles.islandallocators;

import java.util.Collection;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandLookupKey;

/**
 * The linear sparse allocator allocates islands line by line leaving every other island space empty.
 * 
 * <p>If X are already occupied islands, o is space left empty by the allocator and numbers signify
 * the islands returned by this allocator in the order they are returned:</p>
 * 
 * <pre>
 * X X X X X X X X X X X X X X X X
 * X X X X X o 1 o 2 o 3 o 4 o 5 o
 * 6 o 7 ...
 * </pre>
 * 
 * <p>By default each line is 64 islands long. This can be changed by calling {@link #setLineLength(int)}.</p> 
 * 
 * <p><i>Note:</i> The allocator doesn't reuse freeIslands spots, that is the job of the WorldManager to first return
 * islands from this pool and only when it is empty calls this allocator.</p>
 * 
 * @author Catalin Ionescu
 *
 */
public class LinearSparseIslandAllocator extends AbstractIslandAllocator {
	public LinearSparseIslandAllocator() {
		// empty
	}

	@Override
	public void updateCurrentGridOccupied(Collection<Island> islands) {
		for (Island island : islands) {
			if (island.getIslandGridZ() > currentGridZ) {
				currentGridZ = island.getIslandGridZ();
				currentGridX = island.getIslandGridX();
			} else
			if (island.getIslandGridX() > currentGridX) {
				currentGridX = island.getIslandGridX();
			}
		}
		/*
		 * Point to the next spot after last taken island and make sure it's an even spot and within
		 * configured line length.
		 * 
		 */
		currentGridX++;
		if ((currentGridX & 1) == 0) {
			currentGridX++;
		}
		if (currentGridX > lineLength) {
			currentGridX = 0;
			currentGridZ++;
		}
	}
	
	@Override
	public void updateCurrentGridFree(Collection<IslandLookupKey> keys) {
		for (IslandLookupKey key : keys) {
			if (key.getGridZ() > currentGridZ) {
				currentGridZ = key.getGridZ();
				currentGridX = key.getGridX();
			} else
			if (key.getGridX() > currentGridX) {
				currentGridX = key.getGridX();
			}
		}
		/*
		 * Point to the next spot after last taken island and make sure it's an even spot and within
		 * configured line length.
		 * 
		 */
		currentGridX++;
		if ((currentGridX & 1) == 0) {
			currentGridX++;
		}
		if (currentGridX > lineLength) {
			currentGridX = 0;
			currentGridZ++;
		}
	}
	
	/**
	 * Sets the maximum number of islands on each line of the grid to a positive non-zero value
	 * different than the default {@link #DEFAULT_LINE_LENGTH}.
	 * @param lineLength Number of islands for each line of the grid.
	 */
	public void setLineLength(final int lineLength) {
		if (lineLength > 0) {
			this.lineLength = lineLength;
			if (currentGridX > lineLength) {
				currentGridX = 0;
				currentGridZ++;
			}
		}
	}
	
	@Override
	public IslandLookupKey next() {
		IslandLookupKey result = new IslandLookupKey(currentGridX, currentGridZ);
		currentGridX += 2;
		if (currentGridX > lineLength) {
			currentGridX = 0;
			currentGridZ++;
		}
		return result;
	}
	
	/**
	 * DEFAULT_LINE_LENGTH = 64
	 */
	public static final int DEFAULT_LINE_LENGTH = 64;
	
	private int lineLength = DEFAULT_LINE_LENGTH;
	private int currentGridZ = 0;
	private int currentGridX = 0;
}
