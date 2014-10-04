package us.embercraft.emberisles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.World;

import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandLookupKey;
import us.embercraft.emberisles.datatypes.SyncType;
import us.embercraft.emberisles.datatypes.WorldSettings;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.WorldUtils;

public class WorldManager {
	private WorldManager() {
		for (WorldType type : WorldType.values()) {
			islands.put(type, new HashSet<Island>());
			freeIslands.put(type, new HashSet<IslandLookupKey>());
			clearDirty(type);
		}
		rebuildIslandLookupCache();
		rebuildPlayerLookupCache();
		
		for (WorldType type : WorldType.values()) {
			defaultWorldSettings.put(type, new WorldSettings());
			bukkitWorld.put(type, null);
		}
	}
	
	/**
	 * Generates the Bukkit world associated with the configured world type or loads it
	 * from disk if it already exists.
	 * 
	 * <p>This function intended use is to be called from plugin onEnable.</p>
	 * 
	 * @param type World type
	 * @return True on success, false if there was an error
	 */
	public boolean generateBukkitWorld(final WorldType type) {
		if (bukkitWorld.get(type) != null)
			bukkitWorld.put(type, WorldUtils.createWorld(getDefaultWorldSettings(type).getBukkitWorldName(), EmberIsles.getInstance().getWorldGenerator()));
		return true;
	}
	
	public static WorldManager getInstance() {
		if (instance == null)
			instance = new WorldManager();
		return instance;
	}
	
	/**
	 * Discards any existing islands and loads the internal data structures with the provided set.
	 * 
	 * <p>Clears the <i>dirty flag</i> for the specified world.
	 * 
	 * @param type World type
	 * @param islands New set of islands to load
	 */
	@SuppressWarnings("hiding")
	public void addAll(final WorldType type, final Set<Island> islands) {
		this.islands.get(type).clear();
		this.islands.get(type).addAll(islands);
		clearDirty(type);
		rebuildIslandLookupCache();
		rebuildPlayerLookupCache();
	}
	
	/**
	 * Discards all islands from the specified world type. Doesn't clear in-game blocks. 
	 * 
	 * <p>Sets the <i>dirty flag<i> for the specified world.
	 * 
	 * @param type World type
	 */
	public void clear(final WorldType type) {
		islands.get(type).clear();
		setDirty(type);
	}
	
	public Set<Island> getAll(final WorldType type) {
		return islands.get(type);
	}
	
	/**
	 * Returns the <i>dirty flag</i> value for the specified world type.
	 * 
	 * @param type World type
	 * @return The dirty flag value
	 */
	public Boolean isDirty(final WorldType type) {
		return dirtyFlag.get(type);
	}
	
	/**
	 * Sets the <i>dirty flag</i> on for the specified world type.
	 * 
	 * @param type World type
	 */
	public void setDirty(final WorldType type) {
		dirtyFlag.put(type, true);
	}
	
	/**
	 * Clears the <i>dirty flag</i> for the specified world type.
	 * 
	 * @param type World type
	 */
	public void clearDirty(final WorldType type) {
		dirtyFlag.put(type, false);
	}
	
	private void addIslandToLookupCache(final WorldType type, final Island island) {
		if (island == null)
			return;
		IslandLookupKey key = new IslandLookupKey(island);
		if (!islandLookupCache.get(type).containsKey(key)) {
			islandLookupCache.get(type).put(key, new HashSet<Island>());
		}
		islandLookupCache.get(type).get(key).add(island);
	}
	
	private void removeIslandFromLookupCache(final WorldType type, final Island island) {
		if (island == null)
			return;
		IslandLookupKey key = new IslandLookupKey(island);
		if (!islandLookupCache.get(type).containsKey(key)) {
			EmberIsles.getInstance().logInfoMessage("Deleted island not found in lookup cache: " + island);
		} else {
			islandLookupCache.get(type).get(key).remove(island);
			if (islandLookupCache.get(type).get(key).isEmpty()) {
				islandLookupCache.get(type).remove(key);
			}
		}
	}
	
	/**
	 * Initializes and rebuilds the internal fast grid coordinates -> island lookup cache from existing used islands.
	 */
	private void rebuildIslandLookupCache() {
		islandLookupCache.clear();
		for (WorldType type : WorldType.values()) {
			islandLookupCache.put(type, new HashMap<IslandLookupKey, Set<Island>>());
			for (Island island : islands.get(type)) {
				addIslandToLookupCache(type, island);
			}
		}
	}
	
	/**
	 * Adds an island to the world manager for the specified world.
	 * 
	 * <p>Sets the <i>dirty flag</i> on for the specified world.</p>
	 * 
	 * @param type World type
	 * @param island New island to add
	 */
	public void addIsland(final WorldType type, final Island island) {
		islands.get(type).add(island);
		addIslandToLookupCache(type, island);
		syncPlayerLookupCache(type, island, SyncType.ADD);
		setDirty(type);
	}
	
	/**
	 * Removes an island from the world manager for the specified world. The island grid coordinates are added
	 * to the free islands pool for future allocation.
	 * 
	 * <p>Sets the <i>dirty flag</i> on for the specified world.</p>
	 * 
	 * @param type World type
	 * @param island Existing island to remove
	 */
	public void removeIsland(final WorldType type, final Island island) {
		islands.get(type).remove(island);
		freeIslands.get(type).add(new IslandLookupKey(island));
		removeIslandFromLookupCache(type, island);
		syncPlayerLookupCache(type, island, SyncType.REMOVE);
		setDirty(type);
	}
	
	/**
	 * Returns true if there's a player owned island at specified coordinates, false otherwise.
	 * 
	 * @param type World type
	 * @param gridX grid X coordinate
	 * @param gridZ grid Z coordinate
	 * @return
	 */
	public boolean isIslandAtLoc(final WorldType type, final int gridX, final int gridZ) {
		return getIslandAtLoc(type, gridX, gridZ) != null;
	}
	
	/**
	 * Returns the player owned island at specified coordinates or null if there is none.
	 * 
	 * @param type World type
	 * @param gridX grid X coordinate
	 * @param gridZ grid Z coordinate
	 * @return Island owned by players or null if there was none
	 */
	public Island getIslandAtLoc(final WorldType type, final int gridX, final int gridZ) {
		IslandLookupKey key = new IslandLookupKey(gridX, gridZ);
		if (islandLookupCache.get(type).containsKey(key)) {
			for (Island island : islandLookupCache.get(type).get(key)) {
				if (island.getIslandGridX() == gridX && island.getIslandGridZ() == gridZ) {
					return island;
				}
			}
		}
		return null;
	}
	
	/**
	 * Initializes and rebuilds the internal player -> island lookup cache from existing used islands.
	 */
	private void rebuildPlayerLookupCache() {
		playerLookupCache.clear();
		for (WorldType type : WorldType.values()) {
			final Map<UUID, Island> cache = new HashMap<>();
			playerLookupCache.put(type, cache);
			for (Island island : islands.get(type)) {
				cache.put(island.getOwner(), island);
				for (UUID memberId : island.getMembers()) {
					cache.put(memberId, island);
				}
			}
		}
	}
	
	/**
	 * Synchronizes all members / owner of an island with the lookup cache.
	 * 
	 * @param type World type
	 * @param island Island to synchronize members from
	 */
	private void syncPlayerLookupCache(final WorldType type, final Island island, final SyncType syncType) {
		final Map<UUID, Island> cache = playerLookupCache.get(type);
		switch (syncType) {
			case ADD:
				cache.put(island.getOwner(), island);
				for (UUID memberId : island.getMembers()) {
					cache.put(memberId, island);
				}
				break;
			case REMOVE:
				cache.remove(island.getOwner());
				for (UUID memberId : island.getMembers()) {
					cache.remove(memberId);
				}
				break;
		}
	}
	
	/**
	 * Retrieves the island specified player is part of (either as owner or member). Returns null if player is not part of any island.
	 * 
	 * @param type World type
	 * @param playerId Player unique ID
	 * @return Island specified player is part of; null if player is not part of any island
	 */
	public Island getPlayerIsland(final WorldType type, final UUID playerId) {
		return playerLookupCache.get(type).get(playerId);
	}
	
	public void setDefaultWorldSettings(final WorldType type, final WorldSettings settings) {
		defaultWorldSettings.put(type, settings);
	}
	
	public WorldSettings getDefaultWorldSettings(final WorldType type) {
		return defaultWorldSettings.get(type);
	}
	
	/**
	 * Returns true if the player island map for specified world has no entries, false otherwise.
	 * 
	 * @param type World type
	 * @return True if the island map has no entries, false otherwise.
	 */
	public boolean isEmpty(WorldType type) {
		return islands.get(type).isEmpty();
	}
	
	private static WorldManager instance = null;
	
	transient private final Map<WorldType, Map<IslandLookupKey, Set<Island>>> islandLookupCache = new HashMap<>();
	transient private final Map<WorldType, Map<UUID, Island>> playerLookupCache = new HashMap<>();
	transient private final Map<WorldType, Boolean> dirtyFlag = new HashMap<>();
	transient private final Map<WorldType, World> bukkitWorld = new HashMap<>();
	
	private final Map<WorldType, WorldSettings> defaultWorldSettings = new HashMap<>();
	
	private final Map<WorldType, Set<Island>> islands = new HashMap<>();
	private final Map<WorldType, Set<IslandLookupKey>> freeIslands = new HashMap<>();
}
