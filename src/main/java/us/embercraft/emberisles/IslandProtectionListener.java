package us.embercraft.emberisles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import us.embercraft.emberisles.datatypes.Island;

public class IslandProtectionListener implements Listener {
    public IslandProtectionListener(EmberIsles plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock() == null)
            return;
        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock() == null)
            return;
        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.getBlock() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player) || event.getEntity() == null)
            return;

        if (!plugin.getWorldManager().canBuild((Player) event.getRemover(), event.getEntity().getLocation())) {
            ((Player) event.getRemover()).sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBed() == null)
            return;

        if (!plugin.getWorldManager().canUseUtilities(event.getPlayer(), event.getBed().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-utilities"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        if (event.getEgg() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getEgg().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setHatching(false);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.getBlockClicked() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlockClicked().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getBlockClicked() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlockClicked().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player) || event.getVehicle() == null)
            return;

        if (!plugin.getWorldManager().canRide((Player) event.getEntered(), event.getVehicle().getLocation())) {
            ((Player) event.getEntered()).sendMessage(plugin.getMessage("protection-ride"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!(event.getAttacker() instanceof Player) || event.getVehicle() == null)
            return;

        if (!plugin.getWorldManager().canBuild((Player) event.getAttacker(), event.getVehicle().getLocation())) {
            ((Player) event.getAttacker()).sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getBlock() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    /**
     * Called when liquids (lava, water) flow. If the event is cancelled the water will not move.
     * 
     * We check if source and destination blocks are both in same island. If not, we cancel the
     * event to disallow liquids spilling in or out of islands.
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock() == null || event.getToBlock() == null)
            return;

        final Island sourceIsland = plugin.getWorldManager().getIslandAtLoc(event.getBlock().getLocation());
        final Island targetIsland = plugin.getWorldManager().getIslandAtLoc(event.getToBlock().getLocation());

        if (sourceIsland == null && targetIsland == null)
            return;

        if ((sourceIsland != null && !sourceIsland.equals(targetIsland)) ||
                (targetIsland != null && !targetIsland.equals(sourceIsland)))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (event.getBlock() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        if (event.getBlock() == null || !(event.getEntity() instanceof Player))
            return;
        if (!plugin.getWorldManager().canBuild((Player) event.getEntity(), event.getBlock().getLocation())) {
            ((Player) event.getEntity()).sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem() == null)
            return;
        if (!plugin.getWorldManager().canPickItems(event.getPlayer(), event.getItem().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-items"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop() == null)
            return;
        if (!plugin.getWorldManager().canPickItems(event.getPlayer(), event.getItemDrop().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-items"));
            event.setCancelled(true);
        }
    }

    /**
     * Event fired when player right clicks a block.
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        switch (event.getClickedBlock().getType()) {
            case ANVIL:
            case ENCHANTMENT_TABLE:
            case WORKBENCH:
            case BED:
            case ENDER_CHEST:
            case FURNACE:
            case BURNING_FURNACE:
            case BREWING_STAND:
                // INTERACT_ANVILS
                if (!plugin.getWorldManager().canUseUtilities(event.getPlayer(), event.getClickedBlock().getLocation())) {
                    event.getPlayer().sendMessage(plugin.getMessage("protection-utilities"));
                    event.setCancelled(true);
                }
                return;

            case WOODEN_DOOR:
            case FENCE_GATE:
            case TRAP_DOOR:
            case NETHER_FENCE:
                // INTERACT_DOORS
                if (!plugin.getWorldManager().canOpenDoors(event.getPlayer(), event.getClickedBlock().getLocation())) {
                    event.getPlayer().sendMessage(plugin.getMessage("protection-doors"));
                    event.setCancelled(true);
                }
                return;

            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case DIODE:
            case REDSTONE_COMPARATOR:
            case WOOD_PLATE:
            case STONE_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case NOTE_BLOCK:
                // INTERACT_SWITCHES
                if (!plugin.getWorldManager().canInteractSwitches(event.getPlayer(), event.getClickedBlock().getLocation())) {
                    event.getPlayer().sendMessage(plugin.getMessage("protection-switches"));
                    event.setCancelled(true);
                }
                return;

            case CHEST:
            case TRAPPED_CHEST:
            case HOPPER:
            case HOPPER_MINECART:
            case FLOWER_POT:
            case JUKEBOX:
            case ITEM_FRAME:
            case POWERED_MINECART:
            case STORAGE_MINECART:
                // OPEN_CONTAINERS
                if (!plugin.getWorldManager().canOpenChests(event.getPlayer(), event.getClickedBlock().getLocation())) {
                    event.getPlayer().sendMessage(plugin.getMessage("protection-chests"));
                    event.setCancelled(true);
                }
                return;

            default:
                // Keep compiler happy
                break;
        }
    }

    // TODO: Write code

    private EmberIsles plugin;
}
