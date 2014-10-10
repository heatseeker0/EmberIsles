package us.embercraft.emberisles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import us.embercraft.emberisles.datatypes.WorldType;

/**
 * Collection of validators and converters for commonly used command line parameters.
 * 
 * @author Catalin Ionescu
 *
 */
public class CommandHandlerHelpers {
	/**
	 * Transforms a given world type given as string into corresponding WorldType if it exists or null for invalid strings.
	 * @param worldName World type given as string
	 * @return WorldType if it exists or null for invalid strings
	 */
	public static WorldType worldNameToType(String worldName) {
		if (worldName == null) {
			return null;
		}
		try {
			WorldType type = WorldType.getEnum(worldName.toLowerCase());
			return type;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Teleports player to location after 5 ticks.
	 * @param player Player to teleport
	 * @param location Location to teleport to
	 */
	public static void delayedPlayerTeleport(final Player player, final Location location) {
		if (player == null || location == null) {
			return;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(EmberIsles.getInstance(), new Runnable() {
			@Override
			public void run() {
				player.teleport(location, TeleportCause.PLUGIN);
			}
		}, 5L);
	}
}
