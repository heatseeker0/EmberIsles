package us.embercraft.emberisles.util.guimanager;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.util.ItemUtils;
import us.embercraft.emberisles.util.MessageUtils;

public abstract class AbstractGui {
	public AbstractGui(ConfigurationSection config) {
		this.config = config;
		menuTitle = config.getString("title", "No Title");
		configKey = config.getName();
	}
	
	
	/**
	 *  Called automatically by {@link #createGui(Player)} before it populates the Gui with items. Do any general processing here.
	 *  @param player Player this GUI will be sent to
	 */
	protected abstract void preGuiPopulator(final Player player);
	
	/**
	 * Called automatically by {@link #createGui(Player)} after it creates but before it adds a new item to the Gui.
	 * Do any per-item processing here such as custom player based lore.
	 * @param item ItemStack for the new item that has been added.
	 * @param itemKey Configuration key for the new item.
	 * @param player Player this GUI will be sent to
	 * @return True if the item should be added to the Gui.
	 */
	protected abstract boolean onGuiItemAdd(final ItemStack item, final String itemKey, final Player player);
	
	/**
	 * Do action when player selects an item.
	 * @param player Player that selected the item
	 * @param menuTitle The menu title the selected item is part of
	 * @param menuItem The item selected by the player
	 * @return true if the selection was handled by this GUI, false otherwise
	 */
	@SuppressWarnings("hiding")
	public abstract boolean handleMenuSelection(final Player player, final String menuTitle, final ItemStack menuItem);
	
	/**
	 * Creates an inventory type GUI.
	 * @param player Player that this should be sent to.
	 * @return Inventory with the GUI items.
	 */
	public Inventory createGui(Player player) {
		final int guiRows = config.getInt("inventory-rows", 1);
		if (guiRows <= 0 || guiRows > 6) {
			GuiManager.logErrorMessage(String.format("Invalid inventory-rows for GUI '%s'. Check config.yml.", config.getName()));			
			final Inventory gui = Bukkit.createInventory(null, 1 * 9, menuTitle);
			return gui;
		}
		
		final Inventory gui = Bukkit.createInventory(null, guiRows * 9, menuTitle);
		closeOnClick = config.getBoolean("close-on-click", true);
		
		ItemStack item;
		Material material;
		String title;
		List<String> lore;
		
		preGuiPopulator(player);
		ConfigurationSection items = config.getConfigurationSection("items");
		for (String key : items.getKeys(false)) {
			int slot = 0;
			try {
				slot = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				GuiManager.logErrorMessage(String.format("Invalid key node (slot) '%s' in config section '%s'. Must be a number.", key, config.getName()));
				continue;
			}
			
			if ((material = Material.getMaterial(items.getString(key + ".icon"))) == null) {
				GuiManager.logErrorMessage(String.format("Invalid material for item '%s' in config section '%s'. Check config.yml.", key, config.getName()));
				continue;
			}
			
			item = new ItemStack(material);
			if (items.contains(key + ".durability")) {
				item.setDurability((short) items.getInt(key + ".durability"));
			}
			title = MessageUtils.parseColors(items.getString(key + ".title", "Undefined"));
			lore = MessageUtils.parseColors(items.getStringList(key + ".lore"));
			ItemUtils.setItemNameAndLore(item, title, lore);
			if (items.getBoolean(key + ".glow", false)) {
				item.addUnsafeEnchantment(glow, 1);
			}
			if (onGuiItemAdd(item, key, player)) {
				gui.setItem(slot, item);
			}
		}
		
		return gui;
	}
	
	public String getTitle() {
		return menuTitle;
	}
	
	public String getConfigKey() {
		return configKey;
	}
	
	public boolean closeOnClick() {
		return closeOnClick;
	}
	
	public final static Enchantment glow = Enchantment.ARROW_INFINITE;
	protected ConfigurationSection config;
	protected String menuTitle = "No Title";
	private boolean closeOnClick = true;
	protected String configKey = "";
}
