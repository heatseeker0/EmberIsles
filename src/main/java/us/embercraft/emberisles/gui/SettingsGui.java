package us.embercraft.emberisles.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.EmberIsles;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.Pair;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.ItemUtils;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class SettingsGui extends AbstractGui {

    public SettingsGui(ConfigurationSection config) {
        super(config);
    }

    @Override
    protected void preGuiPopulator(Inventory gui, Player player) {
        actions.clear();
    }

    @Override
    protected boolean onGuiItemAdd(ItemStack item, String itemKey, Player player) {
        WorldType type = EmberIsles.getInstance().getGuiWorldType(player);
        SettingsMenuActions action = null;
        if (config.contains("items." + itemKey + ".action")) {
            try {
                action = SettingsMenuActions.valueOf(config.getString("items." + itemKey + ".action"));
            } catch (IllegalArgumentException e) {
                EmberIsles.getInstance().logErrorMessage(String.format("Wrong action in config.yml for key item.%s.action. This menu entry won't be available to players until the error is corrected.", itemKey));
                return false;
            }
        }
        if (type != null && action != null) {
            Island island = EmberIsles.getInstance().getWorldManager().getPlayerIsland(type, player.getUniqueId());
            if (island != null) {
                List<String> lore = ItemUtils.getItemLore(item);
                switch (action) {
                    case TOGGLE_WARP:
                        if (island.isWarpEnabled()) {
                            lore.addAll(MessageUtils.parseColors(config.getStringList("items." + itemKey + ".on-lore")));
                        } else {
                            lore.addAll(MessageUtils.parseColors(config.getStringList("items." + itemKey + ".off-lore")));
                        }
                        ItemUtils.setItemLore(item, lore);
                        break;
                    case TOGGLE_ISLAND_LOCK:
                        if (island.isLocked()) {
                            lore.addAll(MessageUtils.parseColors(config.getStringList("items." + itemKey + ".locked-lore")));
                        } else {
                            lore.addAll(MessageUtils.parseColors(config.getStringList("items." + itemKey + ".unlocked-lore")));
                        }
                        ItemUtils.setItemLore(item, lore);
                        break;
                    default:
                        // Keep compiler happy
                        break;
                }
                actions.put(item, new Pair<>(type, action));
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

        if (actions.containsKey(menuItem)) {
            Pair<WorldType, SettingsMenuActions> pair = actions.get(menuItem);
            switch (pair.getSecond()) {
                case SET_HOME:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "sethome" });
                    break;
                case SET_WARP:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "setwarp" });
                    break;
                case TOGGLE_ISLAND_LOCK:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "togglelock", pair.getFirst().getConfigKey() });
                    break;
                case TOGGLE_WARP:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "togglewarp", pair.getFirst().getConfigKey() });
                    break;
            }
        }
        return true;
    }

    Map<ItemStack, Pair<WorldType, SettingsMenuActions>> actions = new HashMap<>();
}
