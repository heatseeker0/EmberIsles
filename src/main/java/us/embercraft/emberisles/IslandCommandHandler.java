package us.embercraft.emberisles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
		
		if (split.length == 1) {
			switch (split[0].toLowerCase()) {
				case "create":
					islandCreateCmd(player);
					return true;
			}
		}
		return false;
	}
	
	private void islandCreateCmd(final Player player) {
		AbstractGui worldSelector = plugin.getGuiManager().getGuiByKey("world-selector");
		if (worldSelector == null) {
			plugin.logErrorMessage("Gui screen world-selector missing or invalid in config.yml.");
			player.sendMessage(plugin.getMessage("error-internal-misconfigured"));
			return;
		}
		player.openInventory(worldSelector.createGui(player));
	}

	EmberIsles plugin;
}
