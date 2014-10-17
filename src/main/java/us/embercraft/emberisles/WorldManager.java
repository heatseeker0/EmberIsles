package us.embercraft.emberisles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandLookupKey;
import us.embercraft.emberisles.datatypes.SchematicDefinition;
import us.embercraft.emberisles.datatypes.SyncType;
import us.embercraft.emberisles.datatypes.WorldSettings;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.islandallocators.AbstractIslandAllocator;
import us.embercraft.emberisles.islandallocators.LinearSparseIslandAllocator;
import us.embercraft.emberisles.thirdparty.WorldEditAPI;
import us.embercraft.emberisles.util.WorldUtils;

public class WorldManager {
    private WorldManager() {
        for (WorldType type : WorldType.values()) {
            islands.put(type, new HashSet<Island>());
            freeIslands.put(type, new ArrayDeque<IslandLookupKey>());
            clearDirty(type);
        }
        rebuildIslandLookupCache();
        rebuildPlayerLookupCache();

        for (WorldType type : WorldType.values()) {
            defaultWorldSettings.put(type, new WorldSettings());
            schematicDefinitions.put(type, new ArrayList<SchematicDefinition>());
            bukkitWorld.put(type, null);
            islandAllocators.put(type, new LinearSparseIslandAllocator());
        }
    }

    public void initializeAllocator(final WorldType type) {
        LinearSparseIslandAllocator allocator = (LinearSparseIslandAllocator) islandAllocators.get(type);
        allocator.setLineLength(getDefaultWorldSettings(type).getIslandsPerRow());
        allocator.updateCurrentGridOccupied(islands.get(type));
        allocator.updateCurrentGridFree(freeIslands.get(type));
    }

    /**
     * Generates the Bukkit world associated with the configured world type or loads it
     * from disk if it already exists. Also initializes WorldEditAPI internal structures
     * for the loaded / created world.
     * 
     * <p>This function intended use is to be called from plugin onEnable.</p>
     * 
     * @param type World type
     * @return True on success, false if there was an error
     */
    public boolean generateBukkitWorld(final WorldType type) {
        if (bukkitWorld.get(type) == null)
            bukkitWorld.put(type, WorldUtils.createWorld(getDefaultWorldSettings(type).getBukkitWorldName(), EmberIsles.getInstance().getWorldGenerator()));
        if (bukkitWorld.get(type) != null && worldEditAPI.get(type) == null) {
            worldEditAPI.put(type, new WorldEditAPI(bukkitWorld.get(type)));
        }
        return bukkitWorld.get(type) != null && worldEditAPI.get(type) != null;
    }

    public static WorldManager getInstance() {
        if (instance == null)
            instance = new WorldManager();
        return instance;
    }

    /**
     * Discards any existing occupied islands and loads the internal data structures with the provided set.
     * 
     * <p>Clears the <i>dirty flag</i> for the specified world.</p>
     * 
     * @param type World type
     * @param islands New set of islands to load
     */
    @SuppressWarnings("hiding")
    public void addAllOccupied(final WorldType type, final Collection<Island> islands) {
        this.islands.get(type).clear();
        this.islands.get(type).addAll(islands);
        clearDirty(type);
        rebuildIslandLookupCache();
        rebuildPlayerLookupCache();
    }

    /**
     * Discards any existing free islands and loads the internal data structures with the provided set.
     * 
     * <p>
     * Clears the <i>dirty flag</i> for the specified world.
     * 
     * @param type World type
     * @param freeIslandKeys New set of free islands to load
     */
    public void addAllFree(final WorldType type, final Collection<IslandLookupKey> freeIslandKeys) {
        this.freeIslands.get(type).clear();
        this.freeIslands.get(type).addAll(freeIslandKeys);
        clearDirty(type);
    }

    /**
     * Discards all islands from the specified world type. Doesn't clear in-game blocks.
     * 
     * <p>Sets the <i>dirty flag<i> for the specified world.</p>
     * 
     * @param type World type
     */
    public void clear(final WorldType type) {
        islands.get(type).clear();
        islandLookupCache.get(type).clear();
        playerLookupCache.get(type).clear();
        islandAllocators.get(type).clear();
        setDirty(type);
    }

    public Collection<Island> getAllOccupied(final WorldType type) {
        return islands.get(type);
    }

    public Collection<IslandLookupKey> getAllFree(final WorldType type) {
        return freeIslands.get(type);
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
        return getIslandAtLoc(type, new IslandLookupKey(gridX, gridZ));
    }

    /**
     * Returns the player owned island at specified lookup key or null if there is none.
     * 
     * @param type World type
     * @param key Lookup key
     * @return Island owned by players or null if there is none
     */
    public Island getIslandAtLoc(final WorldType type, final IslandLookupKey key) {
        if (type == null || key == null)
            return null;
        if (islandLookupCache.get(type).containsKey(key)) {
            for (Island island : islandLookupCache.get(type).get(key)) {
                if (island.getIslandGridX() == key.getGridX() && island.getIslandGridZ() == key.getGridZ()) {
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

    public void addSchematicDefinition(final WorldType type, SchematicDefinition definition) {
        schematicDefinitions.get(type).add(definition);
    }

    public List<SchematicDefinition> getSchematicDefinitions(final WorldType type) {
        return schematicDefinitions.get(type);
    }

    public void clearSchematicDefinitions(final WorldType type) {
        schematicDefinitions.get(type).clear();
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

    /**
     * Returns the next free island location either from our free islands pool or if the pool
     * is empty, as determined by the automatic island allocation algorithm.
     * 
     * @param type World type
     * @return Next free island location
     */
    public IslandLookupKey getNextFreeIslandLocation(WorldType type) {
        if (freeIslands.get(type).size() > 0) {
            return freeIslands.get(type).pollFirst();
        }
        return islandAllocators.get(type).next();
    }

    /**
     * Transforms grid coordinates into island corner A (top left corner) block coordinates. Y is 0.
     * 
     * @param type World type
     * @param gridX Grid X coordinate
     * @param gridZ Grid Z coordinate
     * @return Island corner A block coordinates
     */
    public Location gridToWorldCoordA(WorldType type, int gridX, int gridZ) {
        final WorldSettings settings = defaultWorldSettings.get(type);
        int locX = gridX * settings.getWorldGranularity();
        int locZ = gridZ * settings.getWorldGranularity();
        return new Location(bukkitWorld.get(type), locX, 0, locZ);
    }

    /**
     * Transforms grid coordinates into island corner B (bottom right) block coordinates. Y is 255.
     * 
     * @param type World type
     * @param gridX Grid X coordinate
     * @param gridZ Grid Z coordinate
     * @return Island corner B block coordinates
     */
    public Location gridToWorldCoordB(WorldType type, int gridX, int gridZ) {
        final WorldSettings settings = defaultWorldSettings.get(type);
        int locX = gridX * settings.getWorldGranularity() + settings.getIslandSize();
        int locZ = gridZ * settings.getWorldGranularity() + settings.getIslandSize();
        return new Location(bukkitWorld.get(type), locX, 255, locZ);
    }

    /**
     * Returns the island corner A (top left corner).
     * 
     * <p>Caches the return value for speed reasons.</p>
     * 
     * @param type World type
     * @param island Island to return the corner of
     * @return Location of island top left corner block
     */
    public Location getIslandCornerA(WorldType type, Island island) {
        if (island.getCornerA() == null)
            island.setCornerA(gridToWorldCoordA(type, island.getIslandGridX(), island.getIslandGridZ()));
        return island.getCornerA();
    }

    /**
     * Returns the island corner B (bottom right corner).
     * 
     * <p>Caches the return value for speed reasons.</p>
     * 
     * @param type World type
     * @param island Island to return the corner of
     * @return Location of island top left corner block
     */
    public Location getIslandCornerB(WorldType type, Island island) {
        if (island.getCornerB() == null)
            island.setCornerB(gridToWorldCoordB(type, island.getIslandGridX(), island.getIslandGridZ()));
        return island.getCornerB();
    }

    /**
     * Returns true if given location is inside the island space. Returns false if location is outside the island
     * space, if island is null or if location is null.
     * 
     * @param type World type
     * @param island Island
     * @param loc Location to check
     * @return True if loc is inside the island space
     */
    public boolean isLocationInIsland(WorldType type, Island island, Location loc) {
        if (island == null || loc == null)
            return false;
        Location cornerA = getIslandCornerA(type, island);
        Location cornerB = getIslandCornerB(type, island);
        return (loc.getX() >= cornerA.getX() && loc.getX() <= cornerB.getX() &&
                loc.getZ() >= cornerB.getZ() && loc.getZ() <= cornerB.getZ());
    }

    /**
     * Given a Bukkit world returns the associated world type or null if this world is not managed by {@link WorldManager}.
     * 
     * @param world Bukkit world
     * @return Associated world type or null if this world is not managed by WorldManager
     */
    public WorldType bukkitWorldToWorldType(final World world) {
        if (world != null && bukkitWorld.values().contains(world)) {
            for (Entry<WorldType, World> entry : bukkitWorld.entrySet()) {
                if (entry.getValue().equals(world)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Given a world type returns the associated Bukkit world.
     * 
     * @param type World type
     * @return Bukkit world
     */
    public World getBukkitWorld(WorldType type) {
        return bukkitWorld.get(type);
    }

    /**
     * Transforms world coordinates into grid coordinates. If the world coordinates map to the space between
     * islands or are outside the island space returns null.
     * 
     * <p>If world type is null then it is calculated by calling {@link #bukkitWorldToWorldType(World)}.
     * For speed reasons it's preferred to use non-null values if possible.</p>
     * 
     * @param type World type or null if not known
     * @param loc World coordinates
     * @return Grid coordinates or null if the coordinates are outside of an island
     */
    public IslandLookupKey worldToGridCoord(WorldType type, final Location loc) {
        if (loc == null) {
            return null;
        }
        if (type == null) {
            type = bukkitWorldToWorldType(loc.getWorld());
            if (type == null) {
                return null;
            }
        }
        int locX = loc.getBlockX();
        int locZ = loc.getBlockZ();
        if (locX < 0 || locZ < 0) {
            return null;
        }
        WorldSettings settings = defaultWorldSettings.get(type);
        if ((locX % settings.getWorldGranularity() <= settings.getIslandSize()) && (locZ % settings.getWorldGranularity() <= settings.getIslandSize())) {
            return new IslandLookupKey(locX / settings.getWorldGranularity(), locZ / settings.getWorldGranularity());
        }
        return null;
    }

    /**
     * Returns the island at given Bukkit world coordinates. Returns null if there's no island at that position.
     * 
     * @param loc Location to return the island for
     * @return Island if there's one or null
     */
    public Island getIslandAtLoc(final Location loc) {
        // TODO: We should cache return values when we'll implement event handlers
        if (loc == null)
            return null;
        WorldType type = bukkitWorldToWorldType(loc.getWorld());
        IslandLookupKey key = worldToGridCoord(type, loc);
        return getIslandAtLoc(type, key);
    }

    /**
     * Retrieves the WorldEdit API for the specified world type.
     * 
     * @param type World type
     * @return WorldEdit API for specified world type
     */
    public WorldEditAPI getWorldEditAPI(WorldType type) {
        return worldEditAPI.get(type);
    }

    /**
     * Removes a member from specified island in the given world type.
     * 
     * @param type World type to work in
     * @param island Island to remove the member from
     * @param recipientId Member to remove
     */
    public void removeIslandMember(WorldType type, Island island, UUID recipientId) {
        if (!island.isMember(recipientId))
            return;

        island.removeMember(recipientId);
        playerLookupCache.get(type).remove(recipientId);
        setDirty(type);
    }

    /**
     * Adds a member to the specified island in the given world type.
     * 
     * @param type World type to work in
     * @param island Island to add the new member to
     * @param recipientId New member to add
     */
    public void addIslandMember(WorldType type, Island island, UUID recipientId) {
        if (island.isMember(recipientId))
            return;

        island.addMember(recipientId);
        playerLookupCache.get(type).put(recipientId, island);
        setDirty(type);
    }

    /**
     * Sets the given island warp at specified location.
     * 
     * @param type World type
     * @param island Island to set the warp for
     * @param loc Location of the new warp
     */
    public void setIslandWarp(WorldType type, Island island, Location loc) {
        island.setWarp(loc);
        setDirty(type);
    }

    /**
     * Toggles the island warp on or off. Returns the new state of the warp.
     * 
     * @param type World type
     * @param island Island to toggle the warp for
     * @return Warp state after the toggle
     */
    public boolean toggleIslandWarp(WorldType type, Island island) {
        if (type == null || island == null)
            return false;
        if (island.isWarpEnabled()) {
            island.disableWarp();
        } else {
            island.enableWarp();
        }
        setDirty(type);
        return island.isWarpEnabled();
    }

    /**
     * Togges the island lock on or off. Returns the new state of the lock.
     * 
     * @param type World type
     * @param island Island to lock or unlock
     * @return New lock state after this command is performed
     */
    public boolean toggleIslandLock(WorldType type, Island island) {
        if (type == null || island == null)
            return false;
        if (island.isLocked()) {
            island.unlock();
        } else {
            island.lock();
        }
        setDirty(type);
        return island.isLocked();
    }

    /**
     * Bans specified player from entering the island by any means. Returns true if the player was banned
     * and false if it was already banned.
     * 
     * @param type World type
     * @param island Island to work with
     * @param playerId Player Id to ban
     * @return True if the player was banned; false if it was already banned
     */
    public boolean banPlayer(WorldType type, Island island, UUID playerId) {
        boolean result = island.banPlayer(playerId);
        if (result) {
            setDirty(type);
        }
        return result;
    }

    /**
     * Unbans specified player from the island.
     * 
     * @param type World type
     * @param island Island to work with
     * @param playerId Player Id to unban
     * @return True if the player was unbanned; false if it wasn't banned to begin with
     */
    public boolean unbanPlayer(WorldType type, Island island, UUID playerId) {
        boolean result = island.unbanPlayer(playerId);
        if (result) {
            setDirty(type);
        }
        return result;
    }

    private static WorldManager instance = null;

    private final Map<WorldType, Map<IslandLookupKey, Set<Island>>> islandLookupCache = new HashMap<>();
    private final Map<WorldType, Map<UUID, Island>> playerLookupCache = new HashMap<>();
    private final Map<WorldType, Boolean> dirtyFlag = new HashMap<>();

    private final Map<WorldType, World> bukkitWorld = new HashMap<>();
    private final Map<WorldType, WorldEditAPI> worldEditAPI = new HashMap<>();

    private final Map<WorldType, WorldSettings> defaultWorldSettings = new HashMap<>();
    private final Map<WorldType, List<SchematicDefinition>> schematicDefinitions = new HashMap<>();

    private final Map<WorldType, AbstractIslandAllocator> islandAllocators = new HashMap<>();

    private final Map<WorldType, Set<Island>> islands = new HashMap<>();
    private final Map<WorldType, Deque<IslandLookupKey>> freeIslands = new HashMap<>();
}
