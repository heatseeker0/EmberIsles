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
					try {
						WorldType type = WorldType.getEnum(split[1].toLowerCase());
						plugin.getWorldManager().clear(type);
						sender.sendMessage(ChatColor.GREEN + "All island data has been deleted for world " + split[1].toLowerCase());
					} catch (IllegalArgumentException e) {
						sender.sendMessage(ChatColor.RED + String.format("Invalid world type %s. Allowed types are normal, challenge, hardcore.", split[1].toLowerCase()));
					}
					return true;
					
				case "testalloc":
					try {
						WorldType type = WorldType.getEnum(split[1].toLowerCase());
						for (int i = 0; i < 10; i++) {
							sender.sendMessage(String.format("Island %d: %s", i, plugin.getWorldManager().getNextFreeIslandLocation(type).toString()));
						}
					} catch (IllegalArgumentException e) {
						sender.sendMessage(ChatColor.RED + String.format("Invalid world type %s. Allowed types are normal, challenge, hardcore.", split[1].toLowerCase()));
					}
					return true;
			}
		}
		return false;
	}

	EmberIsles plugin;
}
