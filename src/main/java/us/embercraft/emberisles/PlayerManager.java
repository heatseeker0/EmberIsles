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
	 * 
	 * <p>Sets the <i>dirty flag</i>.</p>
	 * 
	 * @param uuid
	 * @param string
	 */
	public void addPlayer(final UUID uuid, final String string) {
		players.put(uuid, string);
		playerIdLookup.put(string, uuid);
		setDirty();
	}
	
	/**
	 * Retrieves last known player name for given uuid or an empty string if no match could be found.
	 * @param uuid
	 * @return
	 */
	public String getPlayerName(final UUID uuid) {
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
	 * 
	 * <p>Clears the <i>dirty flag</i>.</p>
	 * @param players
	 */
	@SuppressWarnings("hiding")
	public void addAll(Map<UUID, String> players) {
		this.players.clear();
		this.players.putAll(players);
		rebuildPlayerIdLookup();
		clearDirty();
	}
	
	/**
	 * Removes all mappings from the players map. The map will be empty after this call returns.
	 * 
	 * <p>Sets the <i>dirty flag</i>.</p>
	 */
	public void clear() {
		players.clear();
		rebuildPlayerIdLookup();
		setDirty();
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
	 * Returns the <i>dirty flag</i> value.
	 * @return The dirty flag value
	 */
	public Boolean isDirty() {
		return dirtyFlag;
	}
	
	/**
	 * Sets the <i>dirty flag</i> on.
	 */
	public void setDirty() {
		dirtyFlag = true;
	}
	
	/**
	 * Clears the <i>dirty flag</i>.
	 */
	public void clearDirty() {
		dirtyFlag = false;
	}
	
	/**
	 * Returns true if the players map has no entries, false otherwise.
	 * @return true if the players map has no entries
	 */
	public boolean isEmpty() {
		return players.isEmpty();
	}
	
	private static PlayerManager instance = null;
	transient private boolean dirtyFlag = false;
	transient private Map<String, UUID> playerIdLookup = new HashMap<>();
	
	private final Map<UUID, String> players = new HashMap<>();
}
