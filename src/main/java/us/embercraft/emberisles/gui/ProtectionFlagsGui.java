package us.embercraft.emberisles.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.EmberIsles;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandProtectionAccessGroup;
import us.embercraft.emberisles.datatypes.IslandProtectionFlag;
import us.embercraft.emberisles.datatypes.Triplet;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.ItemUtils;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class ProtectionFlagsGui extends AbstractGui {

    public ProtectionFlagsGui(ConfigurationSection config) {
        super(config);
    }

    @Override
    protected void preGuiPopulator(Inventory gui, Player player) {
        flag.clear();
    }

    @Override
    protected boolean onGuiItemAdd(ItemStack item, String itemKey, Player player) {
        WorldType type = EmberIsles.getInstance().getGuiWorldType(player);
        IslandProtectionAccessGroup accessGroup = null;
        if (config.contains("items." + itemKey + ".access-group")) {
            try {
                accessGroup = IslandProtectionAccessGroup.getEnum(config.getString("items." + itemKey + ".access-group"));
            } catch (IllegalArgumentException e) {
                EmberIsles.getInstance().logErrorMessage(String.format("Wrong action in config.yml for key item.%s.access-group. This menu entry won't be available to players until the error is corrected.", itemKey));
                return false;
            }
        }
        @SuppressWarnings("hiding")
        IslandProtectionFlag flag = null;
        if (config.contains("items." + itemKey + ".flag")) {
            try {
                flag = IslandProtectionFlag.getEnum(config.getString("items." + itemKey + ".flag"));
            } catch (IllegalArgumentException e) {
                EmberIsles.getInstance().logErrorMessage(String.format("Wrong action in config.yml for key item.%s.flag. This menu entry won't be available to players until the error is corrected.", itemKey));
                return false;
            }
        }
        if (type != null && accessGroup != null && flag != null) {
            Island island = EmberIsles.getInstance().getWorldManager().getPlayerIsland(type, player.getUniqueId());
            if (island != null) {
                List<String> lore = ItemUtils.getItemLore(item);
                if (island.getProtectionFlag(accessGroup, flag)) {
                    lore.addAll(MessageUtils.parseColors(config.getStringList("allowed-lore")));
                } else {
                    lore.addAll(MessageUtils.parseColors(config.getStringList("denied-lore")));
                }
                switch (accessGroup) {
                    case MEMBERS:
                        lore.add(ChatColor.GREEN + "Member access");
                        break;
                    case HELPERS:
                        lore.add(ChatColor.GOLD + "Helper access");
                        break;
                    case PUBLIC:
                        lore.add(ChatColor.RED + "Public access");
                        break;
                }
                ItemUtils.setItemLore(item, lore);
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

        return true;
    }

    Map<ItemStack, Triplet<WorldType, IslandProtectionAccessGroup, IslandProtectionFlag>> flag = new HashMap<>();
}
