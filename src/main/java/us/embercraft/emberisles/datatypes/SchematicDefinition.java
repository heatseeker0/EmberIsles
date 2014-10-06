package us.embercraft.emberisles.datatypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicDefinition {
	public SchematicDefinition(WorldType type, String menuTitle, String permission, File schematicFile, List<String> lore, List<String> noPermLore) {
		this.type = type;
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
	private final List<String> lore = new ArrayList<>();
	private final List<String> noPermLore = new ArrayList<>();
	private final String permission;
	private final File schematicFile;
}
