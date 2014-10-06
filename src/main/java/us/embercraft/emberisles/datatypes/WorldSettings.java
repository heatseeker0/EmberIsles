package us.embercraft.emberisles.datatypes;

import org.bukkit.block.Biome;

public class WorldSettings {
	public WorldSettings() {
		/*
		 * Actual values initialized at runtime via config. Sane defaults are provided here in case
		 * the config section for a specific WorldType goes missing.
		 */
		islandSize = 12;
		borderSize = 2;
		y = 128;
		startingBiome = Biome.PLAINS;
		allowParty = true;
		islandsPerRow = 100;
	}
	
	public int getIslandSize() {
		return islandSize;
	}
	public void setIslandSize(int islandSize) {
		this.islandSize = islandSize;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Biome getStartingBiome() {
		return startingBiome;
	}

	public void setStartingBiome(Biome startingBiome) {
		this.startingBiome = startingBiome;
	}

	public boolean getAllowParty() {
		return allowParty;
	}

	public void setAllowParty(boolean allowParty) {
		this.allowParty = allowParty;
	}

	public String getBukkitWorldName() {
		return bukkitWorldName;
	}

	public void setBukkitWorldName(String bukkitWorldName) {
		this.bukkitWorldName = bukkitWorldName;
	}

	public int getIslandsPerRow() {
		return islandsPerRow;
	}

	public void setIslandsPerRow(int islandsPerRow) {
		this.islandsPerRow = islandsPerRow;
	}

	private int islandsPerRow;
	private int islandSize;
	private int borderSize;
	private int y;
	private Biome startingBiome;
	private boolean allowParty;
	private String bukkitWorldName;
}
