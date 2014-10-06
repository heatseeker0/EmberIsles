package us.embercraft.emberisles.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class SchematicSelectorGui extends AbstractGui {

	public SchematicSelectorGui(ConfigurationSection config) {
		super(config);
	}

	@Override
	protected void preGuiPopulator() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean onGuiItemAdd(ItemStack item, String itemKey, Player player) {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("hiding")
	@Override
	public boolean handleMenuSelection(Player player, String menuTitle, ItemStack menuItem) {
		// TODO Auto-generated method stub
		return false;
	}

}
