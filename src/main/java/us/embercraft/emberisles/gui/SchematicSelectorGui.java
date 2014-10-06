package us.embercraft.emberisles.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.EmberIsles;
import us.embercraft.emberisles.datatypes.FutureMenuCommand;
import us.embercraft.emberisles.datatypes.SchematicDefinition;
import us.embercraft.emberisles.util.ItemUtils;
import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class SchematicSelectorGui extends AbstractGui {

	public SchematicSelectorGui(ConfigurationSection config) {
		super(config);
	}

	@Override
	protected void preGuiPopulator(Player player) {
		schematics.clear();
		
		final int availableSlots = config.getInt("inventory-rows", 1) * 9;
		FutureMenuCommand cmd = EmberIsles.getInstance().getFutureCommand(player.getUniqueId());
		if (cmd == null) {
			/*
			 * Player didn't go through WorldSelector first. Nothing we can do about it here except
			 * display an empty schematic selector and let him cancel and start from scratch.
			 */
			return;
		}
		int slot = 0;
		for (SchematicDefinition schematic : EmberIsles.getInstance().getWorldManager().getSchematicDefinitions(cmd.getWorldType())) {		
			ItemStack item = new ItemStack(schematic.getMaterial());
			if (schematic.getDurability() != 0) {
				item.setDurability(schematic.getDurability());
			}
			
			if (player.hasPermission(schematic.getPermission())) {
				ItemUtils.setItemNameAndLore(item, schematic.getMenuTitle(), schematic.getLore());
				schematics.put(item, schematic);
			} else {
				ItemUtils.setItemNameAndLore(item, schematic.getMenuTitle(), schematic.getNoPermLore());
			}
			
			slot++;
			if (slot == availableSlots) {
				break;
			}
		}
	}

	@Override
	protected boolean onGuiItemAdd(ItemStack item, String itemKey, Player player) {
		return true;
	}

	@SuppressWarnings("hiding")
	@Override
	public boolean handleMenuSelection(Player player, String menuTitle, ItemStack menuItem) {
		if (!this.menuTitle.equals(menuTitle))
			return false;
		if (menuItem == null || menuItem.getType() == Material.AIR)
			return true;
		if (schematics.containsKey(menuItem)) {
			FutureMenuCommand cmd = EmberIsles.getInstance().getFutureCommand(player.getUniqueId());
			if (cmd == null) {
				/*
				 * Player didn't go through WorldSelector first. Nothing we can do about it here except
				 * bail out and let him retry.
				 */
				return true;
			}
			cmd.setSchematic(schematics.get(menuItem));
			EmberIsles.getInstance().createIsland(player, cmd);
			EmberIsles.getInstance().deleteFutureCommand(player.getUniqueId());
		}
		return true;
	}

	private Map<ItemStack, SchematicDefinition> schematics = new HashMap<>();
}
