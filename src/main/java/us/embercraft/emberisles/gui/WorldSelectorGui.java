package us.embercraft.emberisles.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.EmberIsles;
import us.embercraft.emberisles.datatypes.FutureMenuCommand;
import us.embercraft.emberisles.datatypes.FutureMenuCommand.CommandType;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.ItemUtils;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class WorldSelectorGui extends AbstractGui {

	public WorldSelectorGui(ConfigurationSection config) {
		super(config);
	}

	@Override
	protected void preGuiPopulator(Player player) {
		worldTypes.clear();
	}

	@Override
	protected boolean onGuiItemAdd(ItemStack item, String itemKey, Player player) {
		if (!player.hasPermission(config.getString("items." + itemKey + ".permission"))) {
			final List<String> lore = MessageUtils.parseColors(config.getStringList("items." + itemKey + ".noperm-lore"));
			ItemUtils.setItemLore(item, lore);
		} else {
			try {
				final WorldType type = WorldType.getEnum(config.getString("items." + itemKey + ".world-type"));
				worldTypes.put(item, type);
			} catch (IllegalArgumentException e) {
				EmberIsles.getInstance().logErrorMessage(String.format("Wrong world type in config.yml world-selector for key item.%s.world-type. This world won't be available to players until this error is corrected.", itemKey));
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("hiding")
	@Override
	public boolean handleMenuSelection(Player player, String menuTitle, ItemStack menuItem) {
		if (!this.menuTitle.equals(menuTitle))
			return false;
		if (menuItem == null || menuItem.getType() == Material.AIR)
			return true;
		if (worldTypes.containsKey(menuItem)) {
			FutureMenuCommand cmd = new FutureMenuCommand(player.getUniqueId(), CommandType.ISLAND_CREATION);
			cmd.setWorldType(worldTypes.get(menuItem));
			EmberIsles.getInstance().addFutureCommand(player.getUniqueId(), cmd);
			EmberIsles.getInstance().getGuiManager().openChainedMenu(player, "schematic-selector");
		}
		
		return true;
	}

	private Map<ItemStack, WorldType> worldTypes = new HashMap<>();
}
