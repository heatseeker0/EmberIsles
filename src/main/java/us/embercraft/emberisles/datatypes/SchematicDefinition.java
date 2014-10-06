package us.embercraft.emberisles.datatypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class SchematicDefinition {
	public SchematicDefinition(WorldType type, Material material, short durability, String menuTitle, String permission, File schematicFile, List<String> lore, List<String> noPermLore) {
		this.type = type;
		this.material = material;
		this.durability = durability;
		this.menuTitle = menuTitle;
		this.permission = permission;
		this.schematicFile = schematicFile;
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
	
	private final WorldType type;
	private final String menuTitle;
	private final Material material;
	private final short durability;
	private final List<String> lore = new ArrayList<>();
	private final List<String> noPermLore = new ArrayList<>();
	private final String permission;
	private final File schematicFile;
}
