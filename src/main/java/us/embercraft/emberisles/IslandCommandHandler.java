package us.embercraft.emberisles;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import us.embercraft.emberisles.datatypes.Invite;
import us.embercraft.emberisles.datatypes.InviteType;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.WorldType;
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
				}
				break;
				
			case 4:
				switch (split[0].toLowerCase()) {
					case "member":
						// /island member <add | remove> <world type> <player name>
						cmdMemberManagement(player, split[1], split[2], split[3]);
						return true;
				}
				break;
		}
		return false;
	}
	
	/**
	 * Accept a pending member or helper invite.
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
				recipient.sendMessage(String.format(plugin.getMessage("member-add-recipient")));
				if (island.getSpawn() != null) {
					CommandHandlerHelpers.delayedPlayerTeleport(recipient, island.getSpawn().toLocation(plugin.getWorldManager().getBukkitWorld(invite.getWorldType())));
				}
				break;
			case ISLAND_ADD_HELPER:
				//TODO: Write code for helpers
				break;
		}
	}

	/**
	 * Create a new island using the automatic island allocator. GUI based.
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
			sender.sendMessage(String.format(plugin.getMessage("member-invite-sent-sender"), playerName, (int) (plugin.getPartyDefinitions().getMemberInviteExpire() / 1000)));
			recipient.sendMessage(String.format(plugin.getMessage("member-invite-sent-recipient"), sender.getName(), sender.getName(),
					(int) (plugin.getPartyDefinitions().getMemberInviteExpire() / 1000)));
			return;
		} else

		if (cmd.equalsIgnoreCase("remove")) {
			if (!island.isMember(recipientId)) {
				sender.sendMessage(String.format(plugin.getMessage("error-not-member"), playerName));
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

	EmberIsles plugin;
}
