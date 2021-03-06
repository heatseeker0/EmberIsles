package us.embercraft.emberisles.datatypes;

import java.util.UUID;

public class FutureMenuCommand {
	public enum CommandType {
		ISLAND_CREATION;
	}
	
	public FutureMenuCommand(UUID playerId, CommandType type) {
		this.playerId = playerId;
		this.type = type;
	}
	
	public CommandType getCommandType() {
		return type;
	}
	
	public UUID getPlayerId() {
		return playerId;
	}
	
	public WorldType getWorldType() {
		return worldType;
	}

	public void setWorldType(WorldType worldType) {
		this.worldType = worldType;
	}

	public SchematicDefinition getSchematic() {
		return schematic;
	}

	public void setSchematic(SchematicDefinition schematic) {
		this.schematic = schematic;
	}

	private final UUID playerId;
	private final CommandType type;
	private WorldType worldType;
	private SchematicDefinition schematic;
}
