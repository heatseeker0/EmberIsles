package us.embercraft.emberisles.util.guimanager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiListener implements Listener {
	public GuiListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInventoryClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();

		final ItemStack clickedItem = event.getCurrentItem();
		if (clickedItem == null || clickedItem.getType() == Material.AIR)
			return;
		
		/*
		 * If it's one of our menus close the inventory and cancel whatever player just did.
		 */
		AbstractGui gui = GuiManager.getInstance().playerMenuSelect(player, event.getView().getTitle(), clickedItem);
		if (gui != null) {
			event.setCancelled(true);
			if (gui.closeOnClick()) {
				/*
				 * Bukkit bug: Inventory closing shouldn't happen in same tick as event processing else bad things happen.
				 */
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run() {
						player.closeInventory();
					}
				}, 1L);
			}
		}
	}
	
	JavaPlugin plugin;
}
