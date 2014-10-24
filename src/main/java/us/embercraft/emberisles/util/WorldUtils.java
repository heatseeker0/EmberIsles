package us.embercraft.emberisles.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class WorldUtils {
    /**
     * Creates a Bukkit world with the specified options. If a world with same name already exists it is
     * loaded from disk and some of the specified options may be ignored.
     * 
     * @param name World name
     * @param generator World generator
     * @param seedString Seed for the new world
     * @param type World type. See {@link org.bukkit.WorldType}
     * @param env World environment. See {@link org.bukkit.World.Environment}
     * @param generateStructures True if structures should be generated (e.g. villages for {@link Environment#NORMAL},
     *            strongholds for {@link Environment#NETHER})
     * @return Newly created world or existing world loaded from disk
     */
    public static World createWorld(String name, String generator, String seedString, WorldType type, Environment env, boolean generateStructures) {
        WorldCreator worldCreator = new WorldCreator(name);
        if (generator != null && !generator.isEmpty()) {
            worldCreator.generator(generator);
        }
        Long seed = null;
        if (seedString != null && seedString.length() > 0) {
            try {
                seed = Long.parseLong(seedString);
            } catch (NumberFormatException e) {
                seed = (long) seedString.hashCode();
            }
            worldCreator.seed(seed);
        }
        if (env == null) {
            env = Environment.NORMAL;
        }
        worldCreator.environment(env);
        if (type == null) {
            type = WorldType.NORMAL;
        }
        worldCreator.type(type);
        worldCreator.generateStructures(generateStructures);

        World world = null;

        try {
            world = worldCreator.createWorld();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return world;
    }

    /**
     * Creates a Bukkit world with the specified options. If a world with same name already exists it is
     * loaded from disk and some of the specified options may be ignored. If it's a new world it will
     * default to {@link Environment#NORMAL}, {@link WorldType#NORMAL}, random seed and no structures
     * will be generated.
     * 
     * <p>For customizable defaults see {@link #createWorld(String, String, String, WorldType, Environment, boolean)}.</p>
     * 
     * @param name World name
     * @param generator World generator
     * @return Newly created world or existing world loaded from disk
     */
    public static World createWorld(String name, String generator) {
        return createWorld(name, generator, null, null, null, false);
    }

    /**
     * Naturally regenerates all chunks in specified area.
     * 
     * @param cornerA Top left corner of area to regenerate
     * @param cornerB Bottom right corner of area to regenerate
     * @return True if regeneration was successful
     */
    public static boolean regenChunks(Location cornerA, Location cornerB) {
        if (cornerA == null || cornerB == null)
            return false;
        boolean result = true;
        final int minChunkX = cornerA.getBlockX() >> 4;
        final int minChunkZ = cornerA.getBlockZ() >> 4;
        final int maxChunkX = cornerB.getBlockX() >> 4;
        final int maxChunkZ = cornerB.getBlockZ() >> 4;
        final World world = cornerA.getWorld();
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                result &= world.regenerateChunk(x, z);
            }
        }
        return result;
    }
}
