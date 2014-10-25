package us.embercraft.emberisles;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.MobType;

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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        if (event.getEntity() == null)
            return;
        if (!plugin.getWorldManager().canInteractFriendly(event.getPlayer(), event.getEntity().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-friendly"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (event.getEntity() == null)
            return;
        switch (CommandHandlerHelpers.entityTypeToMobType(event.getEntity().getType())) {
            case FRIENDLY:
                if (!plugin.getWorldManager().canInteractFriendly(event.getPlayer(), event.getEntity().getLocation())) {
                    event.getPlayer().sendMessage(plugin.getMessage("protection-friendly"));
                    event.setCancelled(true);
                }
                break;
            case HOSTILE:
                if (!plugin.getWorldManager().canInteractHostile(event.getPlayer(), event.getEntity().getLocation())) {
                    event.getPlayer().sendMessage(plugin.getMessage("protection-hostile"));
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() == null || event.getDamager() == null || event.getEntityType() == EntityType.PLAYER)
            return;

        MobType mobType = CommandHandlerHelpers.entityTypeToMobType(event.getEntityType());
        if (mobType == null)
            return;

        Player damager = null;
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Wolf && ((Wolf) event.getDamager()).isTamed()) {
            AnimalTamer tamer = ((Wolf) event.getDamager()).getOwner();
            if (tamer != null) {
                damager = Bukkit.getPlayer(tamer.getUniqueId());
            } else {
                // Untamed wolf
                return;
            }
        } else if (event.getDamager() instanceof Ocelot && ((Ocelot) event.getDamager()).isTamed()) {
            AnimalTamer tamer = ((Ocelot) event.getDamager()).getOwner();
            if (tamer != null) {
                damager = Bukkit.getPlayer(tamer.getUniqueId());
            } else {
                // Untamed ocelot
                return;
            }
        } else if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            } else {
                // Skeleton shooters
                return;
            }
        } else if (event.getDamager() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getDamager();
            if (snowball.getShooter() instanceof Player) {
                damager = (Player) snowball.getShooter();
            } else {
                // Snowman shooter?
                return;
            }
        } else if (event.getDamager() instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) event.getDamager();
            if (potion.getShooter() instanceof Player) {
                damager = (Player) potion.getShooter();
            } else {
                // Witch potion shooter?
                return;
            }
        } else if (event.getDamager() instanceof Projectile) {
            // Generic catch all for other projectiles (fishing bobber, eggs, etc.)
            if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                damager = (Player) ((Projectile) event.getDamager()).getShooter();
            } else {
                return;
            }
        } else {
            return;
        }
        if (damager == null)
            return;

        switch (mobType) {
            case FRIENDLY:
                if (!plugin.getWorldManager().canInteractFriendly(damager, event.getEntity().getLocation())) {
                    damager.sendMessage(plugin.getMessage("protection-friendly"));
                    event.setCancelled(true);
                }
                return;
            case HOSTILE:
                if (!plugin.getWorldManager().canInteractHostile(damager, event.getEntity().getLocation())) {
                    damager.sendMessage(plugin.getMessage("protection-hostile"));
                    event.setCancelled(true);
                }
                return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() == null || !(event.getPotion().getShooter() instanceof Player) ||
                event.getAffectedEntities() == null || event.getAffectedEntities().isEmpty())
            return;
        /*
         * If we want to be comprehensive we should cycle through affected entities, determine each entity class (friendly or hostile),
         * then check the permission for that class. I feel this would be to CPU intensive for minimal benefit.
         * 
         * Instead we only check for interact friendly because presumably if a player has permission to kill friendly mobs on an island
         * then it would have permission to kill hostile mobs too.
         */
        if (!plugin.getWorldManager().canInteractFriendly((Player) event.getPotion().getShooter(), event.getAffectedEntities().iterator().next().getLocation())) {
            ((Player) event.getPotion().getShooter()).sendMessage(plugin.getMessage("protection-friendly"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getPlayer() == null || event.getCaught() == null)
            return;

        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getCaught().getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-friendly"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTame(EntityTameEvent event) {
        if (event.getOwner() == null || event.getEntity() == null)
            return;

        if (event.getOwner() instanceof Player) {
            final Player player = (Player) event.getOwner();
            if (!plugin.getWorldManager().canInteractFriendly(player, event.getEntity().getLocation())) {
                player.sendMessage(plugin.getMessage("protection-friendly"));
                event.setCancelled(true);
            }
        }
    }

    /**
     * Event fired when player right clicks an entity.
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == null || event.getPlayer() == null)
            return;

        final EntityType animal = event.getRightClicked().getType();
        /*
         * Allow all player to player interactions.
         */
        if (animal == EntityType.PLAYER)
            return;

        final ItemStack handItem = event.getPlayer().getItemInHand();
        Material handItemMaterial = null;
        if (handItem != null)
            handItemMaterial = handItem.getType();

        if (!plugin.getWorldManager().canInteractFriendly(event.getPlayer(), event.getRightClicked().getLocation())) {
            // Don't allow villager trading, leash, rename mobs or clone mobs.
            if (animal == EntityType.VILLAGER ||
                    Material.LEASH.equals(handItemMaterial) || Material.NAME_TAG.equals(handItemMaterial) || Material.MONSTER_EGG.equals(handItemMaterial)) {
                event.getPlayer().sendMessage(plugin.getMessage("protection-friendly"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBonemeal(StructureGrowEvent event) {
        if (event.getPlayer() == null || !event.isFromBonemeal())
            return;
        if (!plugin.getWorldManager().canBuild(event.getPlayer(), event.getLocation())) {
            event.getPlayer().sendMessage(plugin.getMessage("protection-build"));
            event.setCancelled(true);
        }
    }

    /**
     * Disallow liquids spilling in or out of islands.
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

    /**
     * No pulling blocks outside our island
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky() || event.getBlock() == null || event.getRetractLocation() == null)
            return;
        final Island pistonIsland = plugin.getWorldManager().getIslandAtLoc(event.getBlock().getLocation());
        final Island targetIsland = plugin.getWorldManager().getIslandAtLoc(event.getRetractLocation());
        if ((pistonIsland != null && !pistonIsland.equals(targetIsland)) ||
                (targetIsland != null && !targetIsland.equals(pistonIsland)))
            event.setCancelled(true);
    }

    /**
     * No pushing blocks from our island
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlock() == null || event.getLength() == 0 || event.getBlock().getRelative(event.getDirection(), event.getLength()) == null)
            return;

        final Island pistonIsland = plugin.getWorldManager().getIslandAtLoc(event.getBlock().getLocation());
        final Island targetIsland = plugin.getWorldManager().getIslandAtLoc(event.getBlock().getRelative(event.getDirection(), event.getLength()).getLocation());
        if ((pistonIsland != null && !pistonIsland.equals(targetIsland)) ||
                (targetIsland != null && !targetIsland.equals(pistonIsland)))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock()))
            return;

        final Island island = plugin.getWorldManager().getIslandAtLoc(event.getTo());
        if (island == null)
            return;
        UUID playerId = event.getPlayer().getUniqueId();
        if (island.isMember(playerId) || island.getOwner().equals(playerId) ||
                plugin.getHelperManager().isHelping(plugin.getWorldManager().bukkitWorldToWorldType(event.getFrom().getWorld()), island.getLookupKey(), playerId))
            return;
        if (island.isLocked() || island.isBanned(playerId)) {
            if (plugin.getWorldManager().isLocationInIsland(plugin.getWorldManager().bukkitWorldToWorldType(event.getFrom().getWorld()), island, event.getFrom())) {
                CommandHandlerHelpers.delayedPlayerTeleport(event.getPlayer(), plugin.getServerSpawn());
            } else {
                event.setTo(event.getFrom());
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("you-were-expelled"));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock()))
            return;

        final Island island = plugin.getWorldManager().getIslandAtLoc(event.getTo());
        if (island == null)
            return;
        UUID playerId = event.getPlayer().getUniqueId();
        if (island.isMember(playerId) || island.getOwner().equals(playerId) ||
                plugin.getHelperManager().isHelping(plugin.getWorldManager().bukkitWorldToWorldType(event.getFrom().getWorld()), island.getLookupKey(), playerId))
            return;
        if (island.isLocked() || island.isBanned(playerId)) {
            if (plugin.getWorldManager().isLocationInIsland(plugin.getWorldManager().bukkitWorldToWorldType(event.getFrom().getWorld()), island, event.getFrom())) {
                CommandHandlerHelpers.delayedPlayerTeleport(event.getPlayer(), plugin.getServerSpawn());
            } else {
                event.setTo(event.getFrom());
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getMessage("you-were-expelled"));
        }
    }

    private EmberIsles plugin;
}
