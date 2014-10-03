package us.embercraft.emberisles.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 
 * Misc helper functions for item names and lore
 * @author Catalin Ionescu <cionescu@gmail.com>
 *
 */
public class ItemUtils {
    public static void setItemNameAndLore(final ItemStack item, final String name, final List<String> lore) {
    	if (item == null)
    		return;
        ItemMeta im = item.getItemMeta();
        if (name != null)
        	im.setDisplayName(name);
        if (lore != null && lore.size() > 0)
        	im.setLore(lore);
        item.setItemMeta(im);
    }
    
    public static void replaceLoreTags(final ItemStack item, final Map<String, String> replacements) {
    	List<String> newLore = new ArrayList<>();
    	for (String line : getItemLore(item)) {
    		for (Entry<String, String> entry : replacements.entrySet()) {
    			line = line.replace(entry.getKey(), entry.getValue());
    		}
    		newLore.add(line);
    	}
    	setItemLore(item, newLore);
    }
    
    public static void setItemName(final ItemStack item, final String name) {
    	if (item == null)
    		return;
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);
    }
    
    public static void setItemLore(final ItemStack item, final List<String> lore) {
    	if (item == null)
    		return;
        ItemMeta im = item.getItemMeta();
        im.setLore(lore);
        item.setItemMeta(im);
    }
    
    public static String getItemName(final ItemStack item) {
    	if (item == null)
    		return "";
    	ItemMeta im = item.getItemMeta();
    	return im != null && im.hasDisplayName() ? im.getDisplayName() : "";
    }
    
    public static List<String> getItemLore(final ItemStack item) {
    	if (item == null)
    		return new ArrayList<>();
    	ItemMeta im = item.getItemMeta();
    	return im != null && im.hasLore() ? im.getLore() : new ArrayList<String>();
    }
}
