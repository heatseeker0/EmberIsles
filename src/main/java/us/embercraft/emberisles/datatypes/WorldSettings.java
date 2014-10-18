package us.embercraft.emberisles.datatypes;

import org.bukkit.block.Biome;

public class WorldSettings {
    public WorldSettings() {
        /*
         * Actual values initialized at runtime via config. Sane defaults are provided here in case
         * the config section for a specific WorldType goes missing.
         */
        islandChunkSize = 12;
        borderChunkSize = 2;
        y = 128;
        startingBiome = Biome.PLAINS;
        allowParty = true;
        allowHelpers = true;
        islandsPerRow = 100;
        updateWorldGranularity();
        updateIslandSize();
    }

    private void updateWorldGranularity() {
        worldGranularity = (islandChunkSize + borderChunkSize) << 4;
    }

    private void updateIslandSize() {
        islandSize = islandChunkSize << 4;
    }

    /**
     * Returns the island size, in chunks.
     * 
     * @return Island size, in chunks
     */
    public int getIslandChunkSize() {
        return islandChunkSize;
    }

    public void setIslandChunkSize(int islandChunkSize) {
        this.islandChunkSize = islandChunkSize;
        updateWorldGranularity();
        updateIslandSize();
    }

    /**
     * Returns the island size in blocks.
     * 
     * @return Island size in blocks
     */
    public int getIslandSize() {
        return islandSize;
    }

    /**
     * Returns the border bettwen islands size, in chunks.
     * 
     * @return Border bettwen islands size, in chunks
     */
    public int getBorderChunkSize() {
        return borderChunkSize;
    }

    public void setBorderChunkSize(int borderChunkSize) {
        this.borderChunkSize = borderChunkSize;
        updateWorldGranularity();
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Retrieves the world granularity, i.e. the number of world blocks between corner A of one island and corner A of next
     * island. It's calculated as <pre>({@link #getIslandChunkSize()} + {@link #getBorderChunkSize()}) * 16</pre> and the result is
     * cached for speed.
     * 
     * @return World granularity
     */
    public int getWorldGranularity() {
        return worldGranularity;
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

    public boolean getAllowHelpers() {
        return allowHelpers;
    }

    public void setAllowHelpers(boolean allowHelpers) {
        this.allowHelpers = allowHelpers;
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

    /*
     * Calculated values
     */
    private int worldGranularity;
    private int islandSize;

    /*
     * Settings
     */
    private int islandsPerRow;
    private int islandChunkSize;
    private int borderChunkSize;
    private int y;
    private Biome startingBiome;
    private boolean allowParty;
    private boolean allowHelpers;
    private String bukkitWorldName;
}
