package us.embercraft.emberisles.datatypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import us.embercraft.emberisles.util.MessageUtils;

public class SchematicDefinition {
	public SchematicDefinition(WorldType type, Material material, short durability, String menuTitle, String permission, File schematicFile, List<String> lore, List<String> noPermLore, Material homeBlockType) {
		this.type = type;
		this.material = material;
		this.durability = durability;
		this.menuTitle = menuTitle;
		schematicName = MessageUtils.stripColors(menuTitle);
		this.permission = permission;
		this.schematicFile = schematicFile;
		this.homeBlockType = homeBlockType;
		if (lore != null) {
			this.lore.addAll(lore);
		}
		if (noPermLore != null) {
			this.noPermLore.addAll(noPermLore);
		}
	}
	
	public WorldType getType() {
		return type;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public short getDurability() {
		return durability;
	}
	
	public String getMenuTitle() {
		return menuTitle;
	}
	
	public List<String> getLore() {
		return lore;
	}
	
	public List<String> getNoPermLore() {
		return noPermLore;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public File getSchematicFile() {
		return schematicFile;
	}
	
	/**
	 * Returns color stripped {@link #getMenuTitle()}.
	 * @return
	 */
	public String getName() {
		return schematicName;
	}
	
	/**
	 * Returns the config setting for the block type that signifies home location should be set there.
	 * @return
	 */
	public Material getHomeBlockType() {
		return homeBlockType;
	}
	
	private final WorldType type;
	private final String menuTitle;
	private final String schematicName;
	private final Material material;
	private final short durability;
	private final List<String> lore = new ArrayList<>();
	private final List<String> noPermLore = new ArrayList<>();
	private final String permission;
	private final File schematicFile;
	private Material homeBlockType;
}
