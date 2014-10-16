package us.embercraft.emberisles;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import us.embercraft.emberisles.datatypes.Helper;
import us.embercraft.emberisles.datatypes.IslandLookupKey;
import us.embercraft.emberisles.datatypes.WorldType;

public class HelperManager {
	private HelperManager() {
		// empty
	}
	
	public static HelperManager getInstance() {
		if (instance == null)
			instance = new HelperManager();
		return instance;
	}
	
	/**
	 * Expired helpers checker. Typical usage is to call this from a runnable every couple seconds.
	 */
	public void helpersExpirerTick() {
		Iterator<Helper> iter = helpers.iterator();
		long currentTime = System.currentTimeMillis();
		while (iter.hasNext()) {
			Helper helper = iter.next();
			if (helper.getExpireTimestamp() <= currentTime) {
				iter.remove();
				removeFromCache(helper);
			}
		}
	}
	
	/**
	 * Adds a helper to an island. Returns true if the helper was added or false if the helper was already added to that island.
	 * @param helper Helper to add
	 * @return True if added, false if it was already part of that island
	 */
	public boolean add(final Helper helper) {
		/*
		 * We deem helpers equal if they have same player ID and are added to the same island.
		 */
		boolean result = helpers.add(helper);
		if (result) {
			if (!islandKeysCache.containsKey(helper.getIslandKey())) {
				islandKeysCache.put(helper.getIslandKey(), new HashSet<Helper>());
			}
			islandKeysCache.get(helper.getIslandKey()).add(helper);
			setDirty();
		}
		return result;
	}
	
	/**
	 * Removes a helper from an island. Returns true if the helper was removed or false if the helper was not part of that island.
	 * @param helper Helper to remove.
	 * @return True if removed, false if it wasn't part of that island
	 */
	public boolean remove(final Helper helper) {
		boolean result = helpers.remove(helper);
		if (result) {
			removeFromCache(helper);
		}
		return result;
	}

	/**
	 * Internally used to remove given helper from cache. Sets dirty flag.
	 * 
	 * <p>Helper <strong>must</strong> be present in the cache or this call can throw a {@link NullPointerException}.
	 * @param helper Helper to remove
	 */
	private void removeFromCache(final Helper helper) throws NullPointerException {
		islandKeysCache.get(helper.getIslandKey()).remove(helper);
		if (islandKeysCache.get(helper.getIslandKey()).isEmpty()) {
			islandKeysCache.remove(helper.getIslandKey());
		}
		setDirty();
	}
	
	public void setDirty() {
		isDirty = true;
	}
	
	public void clearDirty() {
		isDirty = false;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	/**
	 * Returns true if the helpers map has no entries, false otherwise.
	 * @return true if the helpers map has no entries
	 */
	public boolean isEmpty() {
		return helpers.isEmpty();
	}
	
	/**
	 * Loads provided helpers list, discarding any previous entries.
	 * 
	 * <p>Clears the <i>dirty flag</i>.</p>
	 * @param helpers
	 */
	@SuppressWarnings("hiding")
	public void addAll(Collection<Helper> helpers) {
		this.helpers.clear();
		this.helpers.addAll(helpers);
		rebuildCache();
		clearDirty();
	}
	
	/**
	 * Returns the entire helpers list.
	 * @return Helpers collection
	 */
	public Collection<Helper> getAll() {
		return helpers;
	}
	
	/**
	 * Removes all helper invites.
	 */
	public void clear() {
		helpers.clear();
		islandKeysCache.clear();
		setDirty();
	}
	
	private void rebuildCache() {
		for (Helper helper : helpers) {
			if (!islandKeysCache.containsKey(helper.getIslandKey())) {
				islandKeysCache.put(helper.getIslandKey(), new HashSet<Helper>());
			}
			islandKeysCache.get(helper.getIslandKey()).add(helper);
		}
	}
	
	/**
	 * Returns true if there's a helper identified by player UUID for specified island.
	 * @param type World type
	 * @param key Island lookup key
	 * @param playerId Player UUID
	 * @return True if the player is helper on the island
	 */
	public boolean isHelping(final WorldType type, final IslandLookupKey key, final UUID playerId) {
		boolean result = false;
		if (islandKeysCache.containsKey(key)) {
			for (Helper helper : islandKeysCache.get(key)) {
				if (helper.getWorldType() == type && helper.getPlayerId().equals(playerId)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieves a collection of all helpers for specified island.
	 * @param type World type
	 * @param key Island lookup key
	 * @return Collection of helpers for specified island
	 */
	public Collection<Helper> getIslandHelpers(final WorldType type, final IslandLookupKey key) {
		Set<Helper> result = new HashSet<>();
		if (islandKeysCache.containsKey(key)) {
			for (Helper helper : islandKeysCache.get(key)) {
				if (helper.getWorldType() == type) {
					result.add(helper);
				}
			}
		}
		return result;
	}

	private Set<Helper> helpers = new HashSet<>();
	
	private static HelperManager instance = null;
	private boolean isDirty = false;
	private Map<IslandLookupKey, Set<Helper>> islandKeysCache = new HashMap<>();
}
