package us.embercraft.emberisles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import us.embercraft.emberisles.datatypes.IslandProtectionAccessGroup;
import us.embercraft.emberisles.datatypes.IslandProtectionFlag;
import us.embercraft.emberisles.datatypes.MobType;
import us.embercraft.emberisles.datatypes.WorldType;

/**
 * Collection of validators and converters for commonly used command line parameters.
 * 
 * @author Catalin Ionescu
 * 
 */
public class CommandHandlerHelpers {
    /**
     * Transforms a given world type given as string into corresponding WorldType if it exists or null for invalid strings.
     * 
     * @param worldName World type given as string
     * @return WorldType if it exists or null for invalid strings
     */
    public static WorldType worldNameToType(String worldName) {
        if (worldName == null) {
            return null;
        }
        try {
            WorldType type = WorldType.getEnum(worldName.toLowerCase());
            return type;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Transforms a given flag type given as string into corresponding IslandProtectionFlag if it exists or null for invalid strings.
     * 
     * @param flagName Flag name given as string
     * @return IslandProtectionFlag if it exists or null for invalid strings
     */
    public static IslandProtectionFlag flagNameToType(String flagName) {
        if (flagName == null) {
            return null;
        }
        try {
            IslandProtectionFlag flag = IslandProtectionFlag.getEnum(flagName.toLowerCase());
            return flag;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Transforms a given group name given as string into corresponding IslandProtectionAccessGroup if it exists or null for invalid strings.
     * 
     * @param groupName Group name given as string
     * @return IslandProtectionAccessGroup if it exists or null for invalid strings
     */
    public static IslandProtectionAccessGroup groupNameToType(String groupName) {
        if (groupName == null) {
            return null;
        }
        try {
            IslandProtectionAccessGroup flag = IslandProtectionAccessGroup.getEnum(groupName.toLowerCase());
            return flag;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Teleports player to location after 5 ticks.
     * 
     * @param player Player to teleport
     * @param location Location to teleport to
     */
    public static void delayedPlayerTeleport(final Player player, final Location location) {
        if (player == null || location == null) {
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(EmberIsles.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.teleport(location, TeleportCause.PLUGIN);
            }
        }, 5L);
    }

    /**
     * Returns the mob type (friendly, hostile) based on the given entity type.
     * 
     * @param entity Entity type
     * @return Mob type (friendly, hostile)
     */
    public static MobType entityTypeToMobType(EntityType entity) {
        MobType result = null;
        switch (entity) {
            case CREEPER:
            case CAVE_SPIDER:
            case SPIDER:
            case ENDERMAN:
            case GHAST:
            case GIANT:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case ZOMBIE:
            case SLIME:
            case MAGMA_CUBE:
            case WITCH:
            case WITHER:
                result = MobType.HOSTILE;
                break;
            case CHICKEN:
            case COW:
            case PIG:
            case HORSE:
            case WOLF:
            case OCELOT:
            case IRON_GOLEM:
            case SNOWMAN:
            case SHEEP:
            case VILLAGER:
            case MUSHROOM_COW:
                result = MobType.FRIENDLY;
                break;
            default:
                break;
        }
        return result;
    }
}
