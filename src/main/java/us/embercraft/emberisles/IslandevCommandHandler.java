package us.embercraft.emberisles;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import us.embercraft.emberisles.datatypes.Helper;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.WorldType;

public class IslandevCommandHandler implements CommandExecutor {
    public IslandevCommandHandler(EmberIsles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String split[]) {
        switch (split.length) {
            case 1:
                switch (split[0].toLowerCase()) {
                    case "setserverspawn":
                        // islandev setserverspawn - PRODUCTION command
                        cmdSetServerSpawn(sender);
                        return true;
                    case "islandinfo":
                        // islandev islandinfo - DEBUG command
                        cmdIslandInfo(sender);
                        return true;
                    case "fill":
                        // islandev fill - DEBUG command
                        cmdFill(sender);
                        return true;
                    case "clearblocks":
                        // islandev clearblocks - DEBUG command
                        cmdClearBlocks(sender);
                        return true;
                }
                break;
            case 2:
                switch (split[0].toLowerCase()) {
                    case "cleardata":
                        // islandev cleardata <world type> - DEBUG command
                        cmdClearData(sender, split[1]);
                        return true;

                    case "testalloc":
                        // islandev testalloc <world type> - DEBUG command
                        cmdTestAlloc(sender, split[1]);
                        return true;
                }
                break;
        }
        return false;
    }

    /**
     * Clears all in game blocks for the island at sender location. Must be in game to use.
     * 
     * @param sender Admin sending the command
     */
    private void cmdClearBlocks(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "You have to be in game to use this command.");
            return;
        }
        final Player player = (Player) sender;
        Island island = plugin.getWorldManager().getIslandAtLoc(player.getLocation());
        if (island == null) {
            sender.sendMessage(ChatColor.RED + "There is no island at your location.");
            return;
        }
        final Location cornerA = island.getCornerA();
        final Location cornerB = island.getCornerB();
        final int minX = cornerA.getBlockX();
        final int maxX = cornerB.getBlockZ();
        final int minZ = cornerA.getBlockZ();
        final int maxZ = cornerB.getBlockZ();
        final World world = cornerA.getWorld();
        for (int x = minX; x <= maxX; x += 16) {
            for (int z = minZ; z <= maxZ; z += 16) {
                world.regenerateChunk(x, z);
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Clear complete.");
    }

    /**
     * Generates a platform made of smooth stone 1 block high spanning the entire island world space
     * (from cornerA to cornerB) at y 64. The edges are made out of cobblestone.
     * Must be in game to use.
     * 
     * @param sender Admin sending the command
     */
    private void cmdFill(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "You have to be in game to use this command.");
            return;
        }
        final Player player = (Player) sender;
        Island island = plugin.getWorldManager().getIslandAtLoc(player.getLocation());
        if (island == null) {
            sender.sendMessage(ChatColor.RED + "There is no island at your location.");
            return;
        }
        final Location cornerA = island.getCornerA();
        final Location cornerB = island.getCornerB();
        final int minX = cornerA.getBlockX();
        final int maxX = cornerB.getBlockZ();
        final int minZ = cornerA.getBlockZ();
        final int maxZ = cornerB.getBlockZ();
        final World world = cornerA.getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    world.getBlockAt(x, 64, z).setType(Material.COBBLESTONE);
                } else {
                    world.getBlockAt(x, 64, z).setType(Material.STONE);
                }
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Fill complete.");
    }

    /**
     * Displays all island details for the island at sender location. Must be in game to use.
     * 
     * @param sender Admin sending the command
     */
    private void cmdIslandInfo(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "You have to be in game to use this command.");
            return;
        }
        final Player player = (Player) sender;
        Island island = plugin.getWorldManager().getIslandAtLoc(player.getLocation());
        if (island == null) {
            sender.sendMessage(ChatColor.RED + "There is no island at your location.");
            return;
        }

        PlayerManager pm = plugin.getPlayerManager();
        WorldType type = plugin.getWorldManager().bukkitWorldToWorldType(player.getLocation().getWorld());
        sender.sendMessage(ChatColor.GREEN + "World Type: " + ChatColor.GOLD + type.getConfigKey());
        sender.sendMessage(ChatColor.GREEN + "Owner: " + ChatColor.GOLD + pm.getPlayerName(island.getOwner()));
        sender.sendMessage(ChatColor.GREEN + "Schematic: " + ChatColor.GOLD + island.getSchematic());
        sender.sendMessage(ChatColor.GREEN + "Creation Time: " + ChatColor.GOLD + island.getCreateTime());
        sender.sendMessage(ChatColor.GREEN + "Last Activity: " + ChatColor.GOLD + island.getOwnerLoginTime());

        StringBuilder sb = new StringBuilder();
        for (UUID id : island.getMembers()) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(pm.getPlayerName(id));
        }
        sender.sendMessage(ChatColor.GREEN + "Members: " + ChatColor.GOLD + (sb.length() == 0 ? "none" : sb.toString()));

        sb = new StringBuilder();
        for (Helper helper : plugin.getHelperManager().getIslandHelpers(type, island.getLookupKey())) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(pm.getPlayerName(helper.getPlayerId()));
        }
        sender.sendMessage(ChatColor.GREEN + "Helpers: " + ChatColor.GOLD + (sb.length() == 0 ? "none" : sb.toString()));

        sender.sendMessage(ChatColor.GREEN + "Grid Pos: " + ChatColor.GOLD + island.getLookupKey());
        sender.sendMessage(ChatColor.GREEN + "World Pos: A - " + ChatColor.GOLD + island.getCornerA() + ChatColor.GREEN + " B - " + ChatColor.GOLD + island.getCornerB());
        sender.sendMessage(ChatColor.GREEN + "Spawn: " + ChatColor.GOLD + island.getSpawn());
        sender.sendMessage(ChatColor.GREEN + "Warp: " + ChatColor.GOLD + island.getWarp());
        sender.sendMessage(ChatColor.GREEN + "Warp Open: " + ChatColor.GOLD + island.isWarpEnabled());
        sender.sendMessage(ChatColor.GREEN + "Locked: " + ChatColor.GOLD + island.isLocked());
        // TODO: display island flags
    }

    /**
     * Sets the server wide spawn location. Must be in game to run this.
     * 
     * @param sender Admin sending the command
     */
    private void cmdSetServerSpawn(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "You have to be in game to set the server spawn location");
            return;
        }
        plugin.setServerSpawn(((Player) sender).getLocation());
        sender.sendMessage(ChatColor.GREEN + "Server spawn set.");
    }

    /**
     * Deletes all island structures for the specified world type. Doesn't touch in-game blocks. Can be ran from console.
     * 
     * @param sender Admin sending the command
     * @param worldType World type
     */
    private void cmdClearData(CommandSender sender, String worldType) {
        WorldType type = CommandHandlerHelpers.worldNameToType(worldType);
        if (type == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldType.toLowerCase()));
            return;
        }
        plugin.getWorldManager().clear(type);
        sender.sendMessage(ChatColor.GREEN + "All island data has been deleted for world " + worldType.toLowerCase());
    }

    /**
     * Prints the next 10 island locations given by the automatic allocator for the specified world type. Doesn't allocate islands
     * but these locations aren't reused. Can be ran from console.
     * 
     * @param sender Admin sending the command
     * @param worldType World type
     */
    private void cmdTestAlloc(CommandSender sender, String worldType) {
        WorldType type = CommandHandlerHelpers.worldNameToType(worldType);
        if (type == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldType.toLowerCase()));
            return;
        }
        for (int i = 0; i < 10; i++) {
            sender.sendMessage(String.format("Island %d: %s", i, plugin.getWorldManager().getNextFreeIslandLocation(type).toString()));
        }
    }

    EmberIsles plugin;
}
