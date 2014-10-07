package us.embercraft.emberisles.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

/**
 * Misc helper functions for message parsing and colors 
 * @author Catalin Ionescu <cionescu@gmail.com>
 *
 */
public class MessageUtils {
	static public String parseColors(String msg) {
		return msg != null ? ChatColor.translateAlternateColorCodes("&".charAt(0), msg) : "";
	}
	
	
	static public List<String> parseColors(List<String> msgs) {
		if (msgs == null)
			return new ArrayList<>();
		
		List<String> result = new ArrayList<>(msgs.size());
		
		for (String msg : msgs) {
			result.add(ChatColor.translateAlternateColorCodes("&".charAt(0), msg));
		}
		
		return result;
	}
	
	static public List<String> replaceTag(final String tag, final String replacement, List<String> msgs) {
		if (msgs == null)
			return new ArrayList<>();
		List<String> result = new ArrayList<>(msgs.size());
		for (String msg : msgs) {
			result.add(msg.replace(tag, replacement));
		}
		return result;
	}
	
	static public String stripColors(final String msg) {
		return msg != null ? ChatColor.stripColor(msg) : null;
	}
	
	/**
	 * Formats time into human readable form.
	 * @param time time in seconds
	 * @return human readable form such as 5 minutes 30 seconds
	 */
	static public String timeToString(int time) {
		if (time < 0)
			return "less than 1 second";
		
		int hours = time / (60 * 60);
		time = time - hours * 3600;
		int mins = time / 60;
		int sec = time - mins * 60;
		StringBuilder sb = new StringBuilder();
		
		if (hours > 1)
			sb.append(hours + " hours "); else
		if (hours == 1)
			sb.append("1 hour ");
		
		if (mins > 1)
			sb.append(mins + " minutes "); else
		if (mins == 1)
			sb.append("1 minute ");
		
		if (sec > 1)
			sb.append(sec + " seconds"); else
		if (sec == 1)
			sb.append("1 second"); else
		if (mins == 0 && hours == 0)
			sb.append("less than 1 second");
		
		return sb.toString().trim();
	}
}
