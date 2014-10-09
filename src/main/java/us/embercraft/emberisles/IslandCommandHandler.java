package us.embercraft.emberisles;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
			
			case 4:
				switch (split[0].toLowerCase()) {
					case "member":
						/*
						 * /island member <add | remove> <world type> <player name>
						 */
						cmdMemberManagement(player, split[1], split[2], split[3]);
						return true;
				}
		}
		return false;
	}
	
	private void cmdIslandCreate(final Player sender) {
		AbstractGui worldSelector = plugin.getGuiManager().getGuiByKey("world-selector");
		if (worldSelector == null) {
			plugin.logErrorMessage("Gui screen world-selector missing or invalid in config.yml.");
			sender.sendMessage(plugin.getMessage("error-internal-misconfigured"));
			return;
		}
		sender.openInventory(worldSelector.createGui(sender));
	}
	
	private void cmdMemberManagement(final Player sender, String cmd, String worldType, String playerName) {
		WorldType type = CommandHandlerHelpers.worldNameToType(worldType);
		if (type == null) {
			sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldType.toLowerCase()));
			return;
		}
		
		Island island = EmberIsles.getInstance().getWorldManager().getPlayerIsland(type, sender.getUniqueId());
		if (island == null || !island.getOwner().equals(sender.getUniqueId())) {
			sender.sendMessage(plugin.getMessage("error-not-island-owner"));
			return;
		}
		
		UUID recipientId = EmberIsles.getInstance().getPlayerManager().getIdByName(playerName);
		if (recipientId == null) {
			sender.sendMessage(plugin.getMessage("error-player-not-found"));
			return;
		}
	}

	EmberIsles plugin;
}
