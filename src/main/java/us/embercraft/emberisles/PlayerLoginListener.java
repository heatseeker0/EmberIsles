package us.embercraft.emberisles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerLoginListener implements Listener {
	public PlayerLoginListener(EmberIsles plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		// Delayed join to give Lilypad time to propagate the UUID.
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				plugin.onPlayerJoin(player);
			}
    	}, 10L);
	}
	
	EmberIsles plugin;
}
