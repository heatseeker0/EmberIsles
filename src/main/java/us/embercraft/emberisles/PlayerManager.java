package us.embercraft.emberisles;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerManager {
	private PlayerManager() {
		// empty
	}
	
	public static PlayerManager getInstance() {
		if (instance == null)
			instance = new PlayerManager();
		return instance;
	}
	
	/**
	 * Adds a new player to the players map. For existing entries, the player name is updated.
	 * @param uuid
	 * @param string
	 */
	public void addPlayer(final UUID uuid, final String string) {
		players.put(uuid, string);
		playerIdLookup.put(string, uuid);
	}
	
	/**
	 * Retrieves last known player name for given uuid or an empty string if no match could be found.
	 * @param uuid
	 * @return
	 */
	public String getName(final UUID uuid) {
		if (players.containsKey(uuid))
			return players.get(uuid);
		return "";
	}
	
	/**
	 * Retrieves the associated UUID for given player name or null if no match could be found.
	 * @param name
	 * @return
	 */
	public UUID getIdByName(final String name) {
		/*
		 * Rebuilding the cache shouldn't be needed here, but let's play safe.
		 */
		if (playerIdLookup.isEmpty() && !players.isEmpty())
			rebuildPlayerIdLookup();
		return playerIdLookup.get(name);
	}
	
	/**
	 * Loads provided players map, discarding any previous entries.
	 * @param players
	 */
	@SuppressWarnings("hiding")
	public void addAll(Map<UUID, String> players) {
		this.players.clear();
		this.players.putAll(players);
		rebuildPlayerIdLookup();
	}
	
	/**
	 * Removes all mappings from the players map. The map will be empty after this call returns.
	 */
	public void clear() {
		players.clear();
		rebuildPlayerIdLookup();
	}
	
	/**
	 * Rebuilds the internal name -> uuid lookup table.
	 */
	private void rebuildPlayerIdLookup() {
		playerIdLookup.clear();
		for (Entry<UUID, String> entry : players.entrySet()) {
			playerIdLookup.put(entry.getValue(), entry.getKey());
		}
	}
	
	/**
	 * Returns the entire players map.
	 * @return Players uuid -> name map
	 */
	public Map<UUID, String> getAll() {
		return players;
	}
	
	/**
	 * Returns true if the players map has no entries, false otherwise.
	 * @return true if the players map has no entries
	 */
	public boolean isEmpty() {
		return players.isEmpty();
	}
	
	private static PlayerManager instance = null;
	private transient Map<String, UUID> playerIdLookup = new HashMap<>();
	
	private final Map<UUID, String> players = new HashMap<>();
}
