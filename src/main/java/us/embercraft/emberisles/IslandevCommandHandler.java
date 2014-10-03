package us.embercraft.emberisles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class IslandevCommandHandler implements CommandExecutor {
	public IslandevCommandHandler(EmberIsles plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		// TODO
		return false;
	}

	EmberIsles plugin;
}
