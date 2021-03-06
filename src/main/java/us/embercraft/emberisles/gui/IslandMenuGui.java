package us.embercraft.emberisles.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.EmberIsles;
import us.embercraft.emberisles.datatypes.Pair;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class IslandMenuGui extends AbstractGui {

    public IslandMenuGui(ConfigurationSection config) {
        super(config);
    }

    @Override
    protected void preGuiPopulator(Inventory gui, Player player) {
        actions.clear();
    }

    @Override
    protected boolean onGuiItemAdd(ItemStack item, String itemKey, Player player) {
        WorldType type = null;
        if (config.contains("items." + itemKey + ".world-type")) {
            try {
                type = WorldType.getEnum(config.getString("items." + itemKey + ".world-type"));
            } catch (IllegalArgumentException e) {
                EmberIsles.getInstance().logErrorMessage(String.format("Wrong world type in config.yml for key item.%s.world-type. This menu entry won't be available to players until the error is corrected.", itemKey));
                return false;
            }
        }
        TopMenuActions action = null;
        if (config.contains("items." + itemKey + ".action")) {
            try {
                action = TopMenuActions.valueOf(config.getString("items." + itemKey + ".action"));
            } catch (IllegalArgumentException e) {
                EmberIsles.getInstance().logErrorMessage(String.format("Wrong action in config.yml for key item.%s.action. This menu entry won't be available to players until the error is corrected.", itemKey));
                return false;
            }
        }
        if (type != null && action != null) {
            actions.put(item, new Pair<>(type, action));
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
            Pair<WorldType, TopMenuActions> pair = actions.get(menuItem);
            switch (pair.getSecond()) {
                case ISLAND_HOME:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "home", pair.getFirst().getConfigKey() });
                    break;
                case ISLAND_WARP:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "warp", pair.getFirst().getConfigKey(), player.getName() });
                    break;
                case EXPEL:
                    EmberIsles.getInstance().getIslandCmdHandler().onCommand(player, null, null, new String[] { "expel", pair.getFirst().getConfigKey() });
                    break;
                case MANAGE_MEMBERS:
                    player.sendMessage(EmberIsles.getInstance().getMessage("gui-not-implemented"));
                    break;
                case MANAGE_HELPERS:
                    player.sendMessage(EmberIsles.getInstance().getMessage("gui-not-implemented"));
                    break;
                case MANAGE_OWNERSHIP:
                    player.sendMessage(EmberIsles.getInstance().getMessage("gui-not-implemented"));
                    break;
                case ISLAND_SETTINGS:
                    EmberIsles.getInstance().showSettingsGui(player, pair.getFirst());
                    break;
                case ISLAND_PERMISSIONS:
                    EmberIsles.getInstance().showPermissionsGui(player, pair.getFirst());
                    break;
            }
        }

        return true;
    }

    private Map<ItemStack, Pair<WorldType, TopMenuActions>> actions = new HashMap<>();
}
