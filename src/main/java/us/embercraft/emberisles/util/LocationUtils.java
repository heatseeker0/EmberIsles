package us.embercraft.emberisles.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.Vector;

public class LocationUtils {
    /**
     * Parse an input string on the form "world,x,y,z" and an input World to create
     * a Location. This method will only accept strings of the specified form.
     * @param world the World in which the Location exists 
     * @param coords a string on the form "world,x,y,z"
     * @return a Location in the given world with the given coordinates
     */
    public static Location parseSimpleLocation(String coords) {
        String[] parts = coords.split(",");
        if (parts.length != 4)
            throw new IllegalArgumentException("Input string must contain only world, x, y, and z");
        
        final World world = Bukkit.getWorld(parts[0]);
        Integer x   = parseInteger(parts[1]);
        Integer y   = parseInteger(parts[2]);
        Integer z   = parseInteger(parts[3]);
        
        if (x == null || y == null || z == null)
            throw new NullPointerException("Some of the parsed values are null!");
        
        return new Location(world, x, y, z);
    }
    
    /**
     * Create a String representation of a Location object.
     * @param worldName
     * @param loc a Location
     * @return a String of the form "world,x,y,z"
     */
    public static String locationToString(Location loc) {
    	if (loc == null)
    		return "";       
        return String.format("%s,%s,%s,%s", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    
    public static Location parseExactLocation(String coords) {
        String[] parts = coords.split(",");
        if (parts.length != 6)
            throw new IllegalArgumentException("Input string must contain only world, x, y, z, yaw and pitch");
        
        final World world = Bukkit.getWorld(parts[0]);
        Double x   = parseDouble(parts[1]);
        Double y   = parseDouble(parts[2]);
        Double z   = parseDouble(parts[3]);
        Float yaw  = parseFloat(parts[4]);
        Float pitch = parseFloat(parts[5]);
        
        if (x == null || y == null || z == null || yaw == null || pitch == null)
            throw new NullPointerException("Some of the parsed values are null!");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    public static String exactLocationToString(Location loc) {
    	if (loc == null)
    		return "";
    	return String.format("%s,%f,%f,%f,%f,%f", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    
    
    /**
     * Create a String representation of a Vector object.
     * @param worldName
     * @param vec a Vector
     * @return a String of the form "world,x,y,z"
     */
    public static String vectorToString(final String worldName, Vector vec) {
    	if (vec == null)
    		return "";
        return String.format("%s,%s,%s,%s", worldName, vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }
    
    
    /**
     * Converts a WorldEdit Vector into a Bukkit Location.
     * @param worldName
     * @param vec
     * @return
     */
    public static Location vectorToLocation(final String worldName, final Vector vec) {
    	return new Location(Bukkit.getWorld(worldName), vec.getX(), vec.getY(), vec.getZ());
    }
    
    
    public static Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
    
    
    public static Float parseFloat(String s) {
    	try {
    		return Float.parseFloat(s.trim());
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    
    public static Double parseDouble(String s) {
    	try {
    		return Double.parseDouble(s.trim());
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    
	// The player can stand inside these
	public static final Set<Material> HOLLOW_MATERIALS = new HashSet<>(Arrays.asList(
			Material.AIR, Material.SAPLING, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.LONG_GRASS, Material.DEAD_BUSH, Material.YELLOW_FLOWER,
			Material.RED_ROSE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.REDSTONE_WIRE, Material.SEEDS, Material.SIGN_POST,
			Material.WOODEN_DOOR, Material.LADDER, Material.RAILS, Material.WALL_SIGN, Material.LEVER, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK,
			Material.WOOD_PLATE, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.STONE_BUTTON, Material.SNOW, Material.SUGAR_CANE_BLOCK,
			Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.PUMPKIN_STEM, Material.MELON_STEM, Material.VINE, Material.FENCE_GATE, Material.WATER_LILY,
			Material.NETHER_WARTS, Material.CARPET
			));
	
	private static final Set<Material> TRANSPARENT_MATERIALS = new HashSet<>(HOLLOW_MATERIALS);

	static {
		TRANSPARENT_MATERIALS.add(Material.WATER);
		TRANSPARENT_MATERIALS.add(Material.STATIONARY_WATER);
	}
	
	public static final int RADIUS = 3;
	public static final Vector3D[] VOLUME;

	public static class Vector3D {
		public int x;
		public int y;
		public int z;

		public Vector3D(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	static {
		List<Vector3D> pos = new ArrayList<>();
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int y = -RADIUS; y <= RADIUS; y++) {
				for (int z = -RADIUS; z <= RADIUS; z++) {
					pos.add(new Vector3D(x, y, z));
				}
			}
		}
		Collections.sort(pos, new Comparator<Vector3D>() {
			@Override
			public int compare(Vector3D a, Vector3D b) {
				return (a.x * a.x + a.y * a.y + a.z * a.z) - (b.x * b.x + b.y * b.y + b.z * b.z);
			}
		});
		VOLUME = pos.toArray(new Vector3D[0]);
	}

	static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
		if (y > world.getMaxHeight()) {
			return true;
		}
		return HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType());
	}

	public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
		if (isBlockDamaging(world, x, y, z)) {
			return true;
		}
		return isBlockAboveAir(world, x, y, z);
	}

	public static boolean isBlockDamaging(final World world, final int x, final int y, final int z) {
		final Block below = world.getBlockAt(x, y - 1, z);
		if (below.getType() == Material.LAVA || below.getType() == Material.STATIONARY_LAVA) {
			return true;
		}
		if (below.getType() == Material.FIRE) {
			return true;
		}
		if (below.getType() == Material.BED_BLOCK) {
			return true;
		}
		if ((!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z).getType())) || (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).getType()))) {
			return true;
		}
		return false;
	}

	public static Location getSafeDestination(final Location loc) {
		if (loc == null || loc.getWorld() == null) {
			return null;
		}
		final World world = loc.getWorld();
		int x = loc.getBlockX();
		int y = (int) Math.round(loc.getY());
		int z = loc.getBlockZ();
		final int origX = x;
		final int origY = y;
		final int origZ = z;
		while (isBlockAboveAir(world, x, y, z)) {
			y -= 1;
			if (y < 0) {
				y = origY;
				break;
			}
		}
		if (isBlockUnsafe(world, x, y, z)) {
			x = Math.round(loc.getX()) == origX ? x - 1 : x + 1;
			z = Math.round(loc.getZ()) == origZ ? z - 1 : z + 1;
		}
		int i = 0;
		while (isBlockUnsafe(world, x, y, z)) {
			i++;
			if (i >= VOLUME.length) {
				x = origX;
				y = origY + RADIUS;
				z = origZ;
				break;
			}
			x = origX + VOLUME[i].x;
			y = origY + VOLUME[i].y;
			z = origZ + VOLUME[i].z;
		}
		while (isBlockUnsafe(world, x, y, z)) {
			y += 1;
			if (y >= world.getMaxHeight()) {
				x += 1;
				break;
			}
		}
		while (isBlockUnsafe(world, x, y, z)) {
			y -= 1;
			if (y <= 1) {
				x += 1;
				y = world.getHighestBlockYAt(x, z);
				if (x - 48 > loc.getBlockX()) {
					return null;
				}
			}
		}
		return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
	}}
