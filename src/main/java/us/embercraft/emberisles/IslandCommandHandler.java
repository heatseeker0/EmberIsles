package us.embercraft.emberisles;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import us.embercraft.emberisles.datatypes.Helper;
import us.embercraft.emberisles.datatypes.Invite;
import us.embercraft.emberisles.datatypes.InviteType;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandProtectionAccessGroup;
import us.embercraft.emberisles.datatypes.IslandProtectionFlag;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.WorldUtils;
import us.embercraft.emberisles.util.guimanager.AbstractGui;

public class IslandCommandHandler implements CommandExecutor {
    public IslandCommandHandler(EmberIsles plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String split[]) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(plugin.getMessage("error-not-from-console"));
            return true;
        }

        final Player player = (Player) sender;

        switch (split.length) {
            case 0:
                // Show the island top level GUI
                cmdIsland(player);
                break;
            case 1:
                switch (split[0].toLowerCase()) {
                    case "create":
                        /*
                         * /island create
                         * TODO: Perhaps should add command version for these too for consistency
                         * then GUI driven creation
                         */
                        cmdIslandCreate(player);
                        return true;
                    case "setwarp":
                        // /island setwarp - sets the island warp for the island the player is on, provided it is the island owner
                        cmdSetWarp(player);
                        return true;
                }
                break;
            case 2:
                switch (split[0].toLowerCase()) {
                    case "accept":
                        // /island accept <player name>
                        cmdInviteAccept(player, split[1]);
                        return true;
                    case "home":
                        // /island home <world type>
                        cmdHome(player, split[1]);
                        return true;
                    case "expel":
                        // /island expel <world type> [player name] - If player name isn't specified it expels everyone except members, helpers and players with
                        // emberisles.admin.noexpel permission node
                        cmdExpel(player, split[1], null);
                        return true;
                    case "togglewarp":
                        // /island togglewarp <world type> - Toggles their island warp on / off for specified world
                        cmdToggleWarp(player, split[1]);
                        return true;
                    case "lock":
                        // /island lock <world type> - Locks their island so only members and helpers can enter it's space by any means
                        cmdLockUnlock(player, split[1], true);
                        return true;
                    case "unlock":
                        // /island unlock <world type> - Unlock their island
                        cmdLockUnlock(player, split[1], false);
                        return true;
                    case "ban":
                        // /island ban <world type> - Print the list of blacklisted players. All members can see this.
                        cmdBanList(player, split[1]);
                        return true;
                    case "delete":
                        // /island delete <world type>
                        cmdDelete(player, split[1]);
                        return true;
                }
                break;
            case 3:
                switch (split[0].toLowerCase()) {
                    case "expel":
                        // /island expel <world type> [player name] - Expels specified player if not a member, helper and doesn't have emberisles.admin.noexpel
                        cmdExpel(player, split[1], split[2]);
                        return true;
                    case "warp":
                        // /island warp <world type> <player name>
                        cmdWarp(player, split[1], split[2]);
                        return true;
                    case "ban":
                        // /island ban <world type> <player name>
                        cmdBan(player, split[1], split[2]);
                        return true;
                    case "leave":
                        // /island leave <world type> <player name>
                        cmdLeave(player, split[1], split[2]);
                        return true;
                    case "deleteconfirm":
                        // /island deleteconfirm <world type> <confirm code>
                        cmdDeleteConfirm(player, split[1], split[2]);
                        return true;
                    case "transfer":
                        // /island transfer <world type> <player name>
                        cmdTransfer(player, split[1], split[2]);
                        return true;
                }
                break;
            case 4:
                switch (split[0].toLowerCase()) {
                    case "member":
                        // /island member <add | remove> <world type> <player name>
                        cmdMemberManagement(player, split[1], split[2], split[3]);
                        return true;
                    case "helper":
                        // /island helper <add | remove> <world type> <player name> [helper expire time]
                        cmdHelperManagement(player, split[1], split[2], split[3], null);
                        return true;
                }
                break;
            case 5:
                switch (split[0].toLowerCase()) {
                    case "helper":
                        // /island helper <add | remove> <world type> <player name> [helper expire time]
                        cmdHelperManagement(player, split[1], split[2], split[3], split[4]);
                        return true;
                    case "flag":
                        // /island flag <world type> <flag name> <members | helpers | public> <on | off>
                        cmdFlag(player, split[1], split[2], split[3], split[4]);
                        return true;
                }
                break;
        }
        return false;
    }

    private void cmdIsland(Player sender) {
        AbstractGui worldSelector = plugin.getGuiManager().getGuiByKey("island-gui");
        if (worldSelector == null) {
            plugin.logErrorMessage("Gui screen island-gui missing or invalid in config.yml.");
            sender.sendMessage(plugin.getMessage("error-internal-misconfigured"));
            return;
        }
        sender.openInventory(worldSelector.createGui(sender));
    }

    private void cmdTransfer(Player sender, String worldTypeName, String target) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }
        // Target player uuid
        UUID targetId = plugin.getPlayerManager().getIdByName(target);
        if (targetId == null) {
            sender.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }
        // Is target player a member of the island?
        if (!island.isMember(targetId)) {
            sender.sendMessage(String.format(plugin.getMessage("error-not-member-sender"), target));
            return;
        }
        plugin.getWorldManager().transferOwnership(worldType, island, targetId);
        sender.sendMessage(String.format(plugin.getMessage("transfer-complete-sender"), target));
        Player targetPlayer = Bukkit.getPlayer(targetId);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            targetPlayer.sendMessage(plugin.getMessage("transfer-complete-recipient"));
        }
    }

    private void cmdFlag(Player sender, String worldTypeName, String flagName, String accessGroup, String toggle) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Valid flag type?
        IslandProtectionFlag flagType = CommandHandlerHelpers.flagNameToType(flagName);
        if (flagType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-flag-type"), flagName.toLowerCase()));
            return;
        }
        // Valid group?
        IslandProtectionAccessGroup groupType = CommandHandlerHelpers.groupNameToType(accessGroup.toLowerCase());
        if (groupType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-group-type"), accessGroup.toLowerCase()));
            return;
        }
        // Valid flag value?
        boolean flagValue;
        switch (toggle.toLowerCase()) {
            case "on":
            case "true":
                flagValue = true;
                break;
            case "off":
            case "false":
                flagValue = false;
                break;
            default:
                sender.sendMessage(String.format(plugin.getMessage("error-invalid-toggle"), toggle.toLowerCase()));
                return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }
        plugin.getWorldManager().setIslandProtectionFlag(worldType, island, groupType, flagType, flagValue);
        sender.sendMessage(String.format(plugin.getMessage("permission-changed"), flagType.getConfigKey(), groupType.getConfigKey(), MessageUtils.parseColors(flagValue ? "&2ON" : "&cOFF")));
    }

    private void cmdDeleteConfirm(Player sender, String worldTypeName, String confirmCode) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }
        // Valid confirm code?
        if (!plugin.getConfirmCodeManager().isValid(sender, confirmCode)) {
            sender.sendMessage(plugin.getMessage("confirm-code-invalid"));
            return;
        }
        plugin.getConfirmCodeManager().getAndRemove(sender);
        // Teleport all players inside island space to spawn
        if (plugin.getServerSpawn() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!plugin.getWorldManager().isLocationInIsland(worldType, island, player.getLocation())) {
                    continue;
                }
                CommandHandlerHelpers.delayedPlayerTeleport(player, plugin.getServerSpawn());
            }
        }
        for (Helper helper : plugin.getHelperManager().getIslandHelpers(worldType, island.getLookupKey())) {
            plugin.getHelperManager().remove(helper);
        }
        plugin.getWorldManager().removeIsland(worldType, island);
        WorldUtils.regenChunks(island.getCornerA(), island.getCornerB());
        sender.sendMessage(plugin.getMessage("island-deleted"));
    }

    private void cmdDelete(Player sender, String worldTypeName) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }
        // TODO: Add and check cooldown for previous clear / delete
        final String code = plugin.getConfirmCodeManager().generateCode(sender);
        sender.sendMessage(String.format(plugin.getMessage("delete-confirm"), worldTypeName, code));
    }

    /**
     * Member & helpers only, leave current island
     * 
     * @param sender Player wanting to leave
     * @param worldTypeName World type
     * @param target Island owner
     */
    private void cmdLeave(Player sender, String worldTypeName, String target) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        UUID targetId = plugin.getPlayerManager().getIdByName(target);
        if (targetId == null) {
            sender.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }

        // Does the sender belong to an island?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, targetId);
        if (island == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-no-island-target"), target, worldTypeName));
            return;
        }
        // Is sender the island owner?
        if (island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("cant-leave-owner"));
            return;
        }
        // Regular member?
        if (island.isMember(sender.getUniqueId())) {
            plugin.getWorldManager().removeIslandMember(worldType, island, sender.getUniqueId());
            sender.sendMessage(String.format(plugin.getMessage("player-left-sender"), target));
            if (plugin.getServerSpawn() != null && plugin.getWorldManager().isLocationInIsland(worldType, island, sender.getLocation())) {
                CommandHandlerHelpers.delayedPlayerTeleport(sender, plugin.getServerSpawn());
            }
            Player islandOwner = Bukkit.getPlayer(targetId);
            if (islandOwner != null && islandOwner.isOnline()) {
                islandOwner.sendMessage(String.format(plugin.getMessage("player-left-recipient"), sender.getName()));
            }
            return;
        }
        // Helper?
        if (plugin.getHelperManager().isHelping(worldType, island.getLookupKey(), sender.getUniqueId())) {
            plugin.getHelperManager().remove(new Helper(worldType, island.getLookupKey(), sender.getUniqueId(), 0));
            sender.sendMessage(String.format(plugin.getMessage("player-left-sender"), target));
            if (plugin.getServerSpawn() != null && plugin.getWorldManager().isLocationInIsland(worldType, island, sender.getLocation())) {
                CommandHandlerHelpers.delayedPlayerTeleport(sender, plugin.getServerSpawn());
            }
            Player islandOwner = Bukkit.getPlayer(targetId);
            if (islandOwner != null && islandOwner.isOnline()) {
                islandOwner.sendMessage(String.format(plugin.getMessage("player-left-recipient"), sender.getName()));
            }
            return;
        }
        sender.sendMessage(String.format(plugin.getMessage("error-not-member-recipient"), target));
    }

    /**
     * Ban or unban players from entering an island by any means (fly, warp, enderpearl, teleport, etc.).
     * Doesn't allow banning members or helpers.
     * 
     * @param sender Island owner
     * @param worldTypeName World type
     * @param target Player to ban or unban
     */
    private void cmdBan(Player sender, String worldTypeName, String target) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }

        UUID targetId = plugin.getPlayerManager().getIdByName(target);
        if (targetId == null) {
            sender.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }

        // Don't allow banning helpers or members
        if (!island.isBanned(targetId) && island.isMember(targetId)) {
            sender.sendMessage(String.format(plugin.getMessage("error-cant-ban-members"), target));
            return;
        }
        if (!island.isBanned(targetId) && plugin.getHelperManager().isHelping(worldType, island.getLookupKey(), targetId)) {
            sender.sendMessage(String.format(plugin.getMessage("error-cant-ban-helpers"), target));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetId);

        if (island.isBanned(targetId)) {
            plugin.getWorldManager().unbanPlayer(worldType, island, targetId);
            sender.sendMessage(String.format(plugin.getMessage("player-unbanned-sender"), target));
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(String.format(plugin.getMessage("player-unbanned-recipient"), sender.getName()));
            }
        } else {
            plugin.getWorldManager().banPlayer(worldType, island, targetId);
            sender.sendMessage(String.format(plugin.getMessage("player-banned-sender"), target));
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(String.format(plugin.getMessage("player-banned-recipient"), sender.getName()));
                // If banned player is online and inside this island space, send her to server spawn
                if (plugin.getServerSpawn() != null && plugin.getWorldManager().isLocationInIsland(worldType, island, targetPlayer.getLocation())) {
                    CommandHandlerHelpers.delayedPlayerTeleport(targetPlayer, plugin.getServerSpawn());
                    targetPlayer.sendMessage(plugin.getMessage("you-were-expelled"));
                }
            }
        }
    }

    /**
     * Prints the list of blacklisted players. Any member can run this command.
     * 
     * @param sender Island member
     * @param worldTypeName World type
     */
    private void cmdBanList(Player sender, String worldTypeName) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(String.format(plugin.getMessage("error-no-island"), worldType.getConfigKey()));
            return;
        }
        Set<UUID> bannedPlayers = island.getBannedPlayers();
        if (bannedPlayers.isEmpty()) {
            sender.sendMessage(plugin.getMessage("island-blacklist-empty"));
            return;
        }
        sender.sendMessage(plugin.getMessage("island-blacklist-header"));
        StringBuilder sb = new StringBuilder();
        for (UUID uuid : bannedPlayers) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(plugin.getPlayerManager().getPlayerName(uuid));
        }
        sender.sendMessage(String.format(plugin.getMessage("island-blacklist-entry"), sb.toString()));
    }

    /**
     * Locks or unlocks the island. Nobody except members and helpers can enter a locked island space by any means.
     * 
     * @param sender Island owner
     * @param worldTypeName World type
     * @param isLock True to lock the island, false to unlock it
     */
    private void cmdLockUnlock(Player sender, String worldTypeName, boolean isLock) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }
        // Do nothing if state doesn't change to prevent unneeded island DB saves.
        if (isLock && island.isLocked()) {
            sender.sendMessage(plugin.getMessage("island-locked"));
            return;
        }
        if (!isLock && !island.isLocked()) {
            sender.sendMessage(plugin.getMessage("island-unlocked"));
            return;
        }
        if (plugin.getWorldManager().toggleIslandLock(worldType, island)) {
            sender.sendMessage(plugin.getMessage("island-locked"));
        } else {
            sender.sendMessage(plugin.getMessage("island-unlocked"));
        }
    }

    /**
     * Toggles the island warp on or off provided the sender is island owner.
     * 
     * @param sender Island owner
     * @param worldTypeName World type to toggle the warp for
     */
    private void cmdToggleWarp(Player sender, String worldTypeName) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island and is that island owner?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }
        // No warp set? Nothing to toggle.
        if (island.getWarp() == null) {
            sender.sendMessage(plugin.getMessage("error-warp-not-set"));
            return;
        }
        if (plugin.getWorldManager().toggleIslandWarp(worldType, island)) {
            sender.sendMessage(plugin.getMessage("warp-toggle-on"));
        } else {
            sender.sendMessage(plugin.getMessage("warp-toggle-off"));
        }
    }

    /**
     * Sets the island warp at sender location, provided the sender is the island owner and is on her island.
     * 
     * @param sender Island owner
     */
    private void cmdSetWarp(Player sender) {
        final Location senderLoc = sender.getLocation();
        Island island = plugin.getWorldManager().getIslandAtLoc(senderLoc);
        if (island == null) {
            sender.sendMessage(plugin.getMessage("error-not-on-island"));
            return;
        }
        if (!island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-you-must-be-owner"));
            return;
        }
        plugin.getWorldManager().setIslandWarp(plugin.getWorldManager().bukkitWorldToWorldType(senderLoc.getWorld()), island, senderLoc);
        sender.sendMessage(plugin.getMessage("warp-set"));
        // TODO: Maybe inform the owner when their warp is set but not enabled?
    }

    /**
     * Warps to a player owned island warp.
     * 
     * @param sender Player that wants to warp
     * @param worldTypeName World type
     * @param target Target player name
     */
    private void cmdWarp(Player sender, String worldTypeName, String target) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Valid target player name?
        UUID targetPlayerId = plugin.getPlayerManager().getIdByName(target);
        if (targetPlayerId == null) {
            sender.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }
        // Does the sender belong to an island (either as owner or member)?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, targetPlayerId);
        if (island == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-no-island-target"), target, worldType.getConfigKey()));
            return;
        }
        // Do they have an open warp?
        if (!island.isWarpEnabled()) {
            sender.sendMessage(String.format(plugin.getMessage("error-warp-closed"), target));
            return;
        }
        // Is island locked to outsiders?
        if (island.isLocked() && !(island.isMember(sender.getUniqueId()) || plugin.getHelperManager().isHelping(worldType, island.getLookupKey(), sender.getUniqueId()))) {
            sender.sendMessage(String.format(plugin.getMessage("error-island-locked"), target));
            return;
        }
        // Is sender banned on that island?
        if (island.isBanned(sender.getUniqueId())) {
            sender.sendMessage(String.format(plugin.getMessage("error-you-banned"), target));
            return;
        }
        sender.sendMessage(String.format(plugin.getMessage("teleported-warp"), target));
        CommandHandlerHelpers.delayedPlayerTeleport(sender, island.getWarp().toLocation(plugin.getWorldManager().getBukkitWorld(worldType)));
    }

    /**
     * Expels one or more players from sender island. Can't expel members, helpers or players with emberisles.admin.noexpel permission node.
     * 
     * @param sender Owner or island member
     * @param worldTypeName World type (normal, challenge, hardcore)
     * @param target Player name to expel or null to expel all players that match the conditions.
     */
    private void cmdExpel(Player sender, String worldTypeName, String target) {
        // Valid world type?
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Does the sender belong to an island (either as owner or member)?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-no-island"), worldType.getConfigKey()));
            return;
        }
        // If server spawn is not set, bail out
        if (plugin.getServerSpawn() == null) {
            sender.sendMessage(plugin.getMessage("error-server-nospawn"));
            return;
        }
        Set<Player> players = new HashSet<>();
        if (target == null) {
            players.addAll(Bukkit.getOnlinePlayers());
        } else {
            @SuppressWarnings("deprecation")
            final Player player = Bukkit.getPlayer(target);
            if (player != null && player.isOnline()) {
                players.add(player);
            } else {
                sender.sendMessage(plugin.getMessage("error-player-not-found"));
                return;
            }
        }
        int count = 0;
        for (Player player : players) {
            UUID playerId = player.getUniqueId();
            if (playerId.equals(island.getOwner()) || island.isMember(playerId) || plugin.getHelperManager().isHelping(worldType, island.getLookupKey(), playerId) ||
                    player.hasPermission("emberisles.admin.noexpel") || !plugin.getWorldManager().isLocationInIsland(worldType, island, player.getLocation())) {
                continue;
            }
            CommandHandlerHelpers.delayedPlayerTeleport(player, plugin.getServerSpawn());
            player.sendMessage(plugin.getMessage("you-were-expelled"));
            count++;
        }
        if (count > 0) {
            sender.sendMessage(String.format(plugin.getMessage("expelled-count"), count));
        } else {
            sender.sendMessage(plugin.getMessage("expelled-none"));
        }
    }

    /**
     * Teleport a player to her island home in specified world.
     * 
     * @param sender Player to teleport
     * @param string World type (normal, challenge, hardcore)
     */
    private void cmdHome(Player sender, String worldTypeName) {
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-no-island"), worldType.getConfigKey()));
            return;
        }
        sender.sendMessage(plugin.getMessage("teleported-home"));
        CommandHandlerHelpers.delayedPlayerTeleport(sender, island.getSpawn().toLocation(plugin.getWorldManager().getBukkitWorld(worldType)));
    }

    /**
     * Accept a pending member or helper invite.
     * 
     * @param recipient Player that is accepting the invite
     * @param senderName Player name that sent the invite
     */
    private void cmdInviteAccept(final Player recipient, String senderName) {
        UUID sender = plugin.getPlayerManager().getIdByName(senderName);
        if (sender == null) {
            recipient.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }

        Invite invite = plugin.getInviteManager().getPlayerInviteExact(sender, recipient.getUniqueId());
        if (invite == null) {
            recipient.sendMessage(String.format(plugin.getMessage("error-no-invite"), senderName));
            return;
        }

        Island island = plugin.getWorldManager().getPlayerIsland(invite.getWorldType(), invite.getSender());
        // Sender no longer has an island
        if (island == null) {
            recipient.sendMessage(plugin.getMessage("error-invalid-invite"));
            plugin.getInviteManager().remove(invite);
            return;
        }
        // Is sender online? Some permission managers don't return correct or complete permissions for offline players
        final Player senderPlayer = Bukkit.getPlayer(sender);
        if (senderPlayer == null || !senderPlayer.isOnline()) {
            recipient.sendMessage(String.format(plugin.getMessage("error-player-not-online"), senderName));
            plugin.getInviteManager().remove(invite);
            return;
        }
        switch (invite.getInviteType()) {
            case ISLAND_ADD_MEMBER:
                // Recipient already member or owner of another island?
                if (plugin.getWorldManager().getPlayerIsland(invite.getWorldType(), recipient.getUniqueId()) != null) {
                    recipient.sendMessage(plugin.getMessage("error-you-another-island"));
                    plugin.getInviteManager().clearPlayerInvites(invite.getWorldType(), InviteType.ISLAND_ADD_MEMBER, recipient.getUniqueId());
                    return;
                }
                // Sender over party limit
                if (plugin.getPartyDefinitions().getMaxPartyLimit(senderPlayer) <= island.getMembers().size()) {
                    recipient.sendMessage(plugin.getMessage("error-invalid-invite"));
                    plugin.getInviteManager().remove(invite);
                    return;
                }
                plugin.getWorldManager().addIslandMember(invite.getWorldType(), island, recipient.getUniqueId());
                plugin.getInviteManager().clearPlayerInvites(invite.getWorldType(), InviteType.ISLAND_ADD_MEMBER, recipient.getUniqueId());
                senderPlayer.sendMessage(String.format(plugin.getMessage("member-add-sender"), recipient.getName()));
                recipient.sendMessage(String.format(plugin.getMessage("member-add-recipient"), senderPlayer.getName()));
                if (island.getSpawn() != null) {
                    recipient.sendMessage(plugin.getMessage("teleported-home"));
                    CommandHandlerHelpers.delayedPlayerTeleport(recipient, island.getSpawn().toLocation(plugin.getWorldManager().getBukkitWorld(invite.getWorldType())));
                }
                break;
            case ISLAND_ADD_HELPER:
                // Recipient already helper on this island
                if (plugin.getHelperManager().isHelping(invite.getWorldType(), island.getLookupKey(), recipient.getUniqueId())) {
                    recipient.sendMessage(String.format(plugin.getMessage("error-already-helper-recipient"), senderName));
                    plugin.getInviteManager().remove(invite);
                    return;
                }
                plugin.getHelperManager().add(new Helper(invite.getWorldType(), island.getLookupKey(), recipient.getUniqueId(), invite.getHelperDuration()));
                plugin.getInviteManager().remove(invite);
                senderPlayer.sendMessage(String.format(plugin.getMessage("helper-add-sender"), recipient.getName()));
                recipient.sendMessage(String.format(plugin.getMessage("helper-add-recipient"), senderPlayer.getName()));
                break;
        }
    }

    /**
     * Create a new island using the automatic island allocator. GUI based.
     * 
     * @param sender Future island owner
     */
    private void cmdIslandCreate(final Player sender) {
        AbstractGui worldSelector = plugin.getGuiManager().getGuiByKey("world-selector");
        if (worldSelector == null) {
            plugin.logErrorMessage("Gui screen world-selector missing or invalid in config.yml.");
            sender.sendMessage(plugin.getMessage("error-internal-misconfigured"));
            return;
        }
        sender.openInventory(worldSelector.createGui(sender));
    }

    /**
     * Add or remove island members.
     * 
     * @param sender Island owner
     * @param cmd Command (add or remove)
     * @param worldTypeName World type for this action
     * @param playerName Member name to add or remove
     */
    private void cmdMemberManagement(final Player sender, String cmd, String worldTypeName, String playerName) {
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Sender owns an island?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }

        UUID recipientId = plugin.getPlayerManager().getIdByName(playerName);
        if (recipientId == null) {
            sender.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }

        if (cmd.equalsIgnoreCase("add")) {
            // World settings don't allow parties
            if (!plugin.getWorldManager().getDefaultWorldSettings(worldType).getAllowParty()) {
                sender.sendMessage(plugin.getMessage("error-world-noparty"));
                return;
            }
            // Recipient is already a member on sender island
            if (island.isMember(recipientId)) {
                sender.sendMessage(String.format(plugin.getMessage("error-already-member"), playerName));
                return;
            }
            // Recipient owns or is member on another island
            if (plugin.getWorldManager().getPlayerIsland(worldType, recipientId) != null) {
                sender.sendMessage(String.format(plugin.getMessage("error-has-another-island"), playerName));
                return;
            }
            // Sender over party limit
            if (plugin.getPartyDefinitions().getMaxPartyLimit(sender) <= island.getMembers().size()) {
                sender.sendMessage(plugin.getMessage("error-party-limit"));
                return;
            }
            /*
             * To simplify things for the players, you can't simultaneously send an invite for say both your normal
             * and challenge world to the same player. She has to accept your first invite (e.g. for the normal
             * world) before you can send the other one for the challenge world. Same for helper and member.
             */
            if (plugin.getInviteManager().hasInvite(sender.getUniqueId(), recipientId)) {
                sender.sendMessage(String.format(plugin.getMessage("error-already-pending"), playerName));
                return;
            }
            // Is recipient online? (won't add offline players since they can't accept invites)
            final Player recipient = Bukkit.getPlayer(recipientId);
            if (recipient == null || !recipient.isOnline()) {
                sender.sendMessage(String.format(plugin.getMessage("error-player-not-online"), playerName));
                return;
            }

            Invite invite = new Invite(worldType, InviteType.ISLAND_ADD_MEMBER, sender.getUniqueId(), recipientId);
            plugin.getInviteManager().add(invite);
            sender.sendMessage(String.format(plugin.getMessage("member-invite-sent-sender"), playerName, (int) (plugin.getPartyDefinitions().getMemberInviteExpire() / EmberIsles.MILLISECONDS_PER_SECOND)));
            recipient.sendMessage(String.format(plugin.getMessage("member-invite-sent-recipient"), sender.getName(), sender.getName(), (int) (plugin.getPartyDefinitions().getMemberInviteExpire() / EmberIsles.MILLISECONDS_PER_SECOND)));
            return;
        } else

        if (cmd.equalsIgnoreCase("remove")) {
            if (!island.isMember(recipientId)) {
                sender.sendMessage(String.format(plugin.getMessage("error-not-member-sender"), playerName));
                return;
            }

            plugin.getWorldManager().removeIslandMember(worldType, island, recipientId);
            sender.sendMessage(String.format(plugin.getMessage("member-remove-sender"), playerName));

            final Player recipient = Bukkit.getPlayer(recipientId);
            if (recipient != null && recipient.isOnline()) {
                recipient.sendMessage(String.format(plugin.getMessage("member-remove-recipient"), sender.getName()));
            }
            return;
        }

        // Not add or remove, it's an error. Print short help on /island member
        sender.sendMessage(plugin.getMessage("island-member-help"));
    }

    /**
     * Add or remove island helpers.
     * 
     * @param sender Island owner
     * @param cmd Command (add or remove)
     * @param worldTypeName World type for this action
     * @param playerName Helper name to add or remove
     * @param helperDuration Time this helper is to be added in MINUTES (null for remove operations or to use default config specified time)
     */
    private void cmdHelperManagement(Player sender, String cmd, String worldTypeName, String playerName, String helperDuration) {
        WorldType worldType = CommandHandlerHelpers.worldNameToType(worldTypeName);
        if (worldType == null) {
            sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldTypeName.toLowerCase()));
            return;
        }
        // Sender owns an island?
        Island island = plugin.getWorldManager().getPlayerIsland(worldType, sender.getUniqueId());
        if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
            sender.sendMessage(plugin.getMessage("error-not-island-owner"));
            return;
        }

        UUID recipientId = plugin.getPlayerManager().getIdByName(playerName);
        if (recipientId == null) {
            sender.sendMessage(plugin.getMessage("error-player-not-found"));
            return;
        }

        if (cmd.equalsIgnoreCase("add")) {
            // World settings don't allow parties
            if (!plugin.getWorldManager().getDefaultWorldSettings(worldType).getAllowHelpers()) {
                sender.sendMessage(plugin.getMessage("error-world-nohelpers"));
                return;
            }

            long helperDurationMs = plugin.getPartyDefinitions().getHelperDefaultDuration();
            if (helperDuration != null) {
                try {
                    // TODO: Maybe in future allow input string flexibility such as 1h30m. Minor priority since most players will use GUI predefined values.
                    helperDurationMs = Integer.parseInt(helperDuration) * EmberIsles.MILLISECONDS_PER_MINUTE;
                } catch (NumberFormatException e) {
                    helperDurationMs = plugin.getPartyDefinitions().getHelperDefaultDuration();
                }
            }
            // Recipient is already a helper on sender island
            if (plugin.getHelperManager().isHelping(worldType, island.getLookupKey(), recipientId)) {
                sender.sendMessage(String.format(plugin.getMessage("error-already-helper"), playerName));
                return;
            }

            /*
             * To simplify things for the players, you can't simultaneously send an invite for say both your normal
             * and challenge world to the same player. She has to accept your first invite (e.g. for the normal
             * world) before you can send the other one for the challenge world. Same for helper and member.
             */
            if (plugin.getInviteManager().hasInvite(sender.getUniqueId(), recipientId)) {
                sender.sendMessage(String.format(plugin.getMessage("error-already-pending"), playerName));
                return;
            }
            // Is recipient online? (won't add offline players since they can't accept invites)
            final Player recipient = Bukkit.getPlayer(recipientId);
            if (recipient == null || !recipient.isOnline()) {
                sender.sendMessage(String.format(plugin.getMessage("error-player-not-online"), playerName));
                return;
            }

            Invite invite = new Invite(worldType, InviteType.ISLAND_ADD_HELPER, sender.getUniqueId(), recipientId, helperDurationMs);
            plugin.getInviteManager().add(invite);
            sender.sendMessage(String.format(plugin.getMessage("helper-invite-sent-sender"), playerName, (int) (plugin.getPartyDefinitions().getHelperInviteExpire() / EmberIsles.MILLISECONDS_PER_SECOND)));
            recipient.sendMessage(String.format(plugin.getMessage("helper-invite-sent-recipient"), sender.getName(), (int) (helperDurationMs / EmberIsles.MILLISECONDS_PER_MINUTE), sender.getName(), (int) (plugin.getPartyDefinitions().getHelperInviteExpire() / EmberIsles.MILLISECONDS_PER_SECOND)));
            return;
        } else

        if (cmd.equalsIgnoreCase("remove")) {
            if (!plugin.getHelperManager().isHelping(worldType, island.getLookupKey(), recipientId)) {
                sender.sendMessage(String.format(plugin.getMessage("error-not-helper"), playerName));
                return;
            }

            plugin.getHelperManager().remove(new Helper(worldType, island.getLookupKey(), recipientId, 0));
            sender.sendMessage(String.format(plugin.getMessage("helper-remove-sender"), playerName));

            final Player recipient = Bukkit.getPlayer(recipientId);
            if (recipient != null && recipient.isOnline()) {
                recipient.sendMessage(String.format(plugin.getMessage("helper-remove-recipient"), sender.getName()));
            }
            return;
        }

        // Not add or remove, it's an error. Print short help on /island member
        sender.sendMessage(plugin.getMessage("island-helper-help"));
    }

    EmberIsles plugin;
}
