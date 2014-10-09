package us.embercraft.emberisles;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import us.embercraft.emberisles.datatypes.WorldType;

public class IslandevCommandHandler implements CommandExecutor {
	public IslandevCommandHandler(EmberIsles plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String split[]) {
		if (split.length == 2) {
			switch (split[0].toLowerCase()) {
				case "clear":
					cmdClear(sender, split[1]);
					return true;

				case "testalloc":
					cmdTestAlloc(sender, split[1]);
					return true;
			}
		}
		return false;
	}

	private void cmdClear(CommandSender sender, String worldType) {
		WorldType type = CommandHandlerHelpers.worldNameToType(worldType);
		if (type == null) {
			sender.sendMessage(String.format(plugin.getMessage("error-invalid-world-type"), worldType.toLowerCase()));
			return;
		}
		plugin.getWorldManager().clear(type);
		sender.sendMessage(ChatColor.GREEN + "All island data has been deleted for world " + worldType.toLowerCase());
	}

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
