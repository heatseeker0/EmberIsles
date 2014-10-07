package us.embercraft.emberisles.thirdparty;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class WorldEditAPI {
	/**
	 * Initializes the link to WorldEdit. Returns true on success. Method should be called once per plugin, before
	 * creating any instances of this class.
	 * @param printStackTraces True if exception stack traces should be output on error. Set to false for production use.
	 * @return True on success
	 */
	@SuppressWarnings("hiding")
	public static boolean initAPI(boolean printStackTraces) {
		plugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		WorldEditAPI.printStackTraces = printStackTraces;
		if (plugin != null && plugin.isEnabled()) {
			worldEdit = plugin.getWorldEdit();
		}
		return isInitialized();
	}
	
	/**
	 * Returns true if the WorldEditAPI is successfully set up and ready for use.
	 * @return True if the WorldEditAPI is ready for use.
	 */
	public static boolean isInitialized() {
		return worldEdit != null;
	}
	
	public WorldEditAPI(World world) {
		if (!isInitialized() && !initAPI(false)) {
			throw new IllegalStateException("Couldn't initialize WorldEditAPI");
		}
		localSession = new LocalSession(worldEdit.getConfiguration());
		editSession = new EditSession(new BukkitWorld(world), worldEdit.getConfiguration().maxChangeLimit);
	}
	
	/**
	 * Pastes a schematic optionally centering it around specified paste location. If the paste location is null
	 * the schematic will be pasted at the original point it was taken that is saved within the schematic.
	 * 
	 * @param schematic File containing the schematic
	 * @param pasteLoc In-game location the schematic will be pasted. If null schematic will be pasted at the original location it was taken from.
	 * @param centered True to paste the schematic centered around specified non-null paste location. If the paste location is null this setting is ignored.
	 * @return True on success
	 */
	public boolean pasteSchematic(final File schematic, Location pasteLoc, boolean centered) {
		try {
			final File file = worldEdit.getSafeSaveFile(null, schematic.getParentFile(), schematic.getName(), EXTENSION, new String[] { EXTENSION });

			editSession.enableQueue();
			localSession.setClipboard(SchematicFormat.MCEDIT.load(file));
			Vector pasteLocation = localSession.getClipboard().getOrigin();
			if (pasteLoc != null) {
				pasteLocation = new Vector(pasteLoc.getBlockX(), pasteLoc.getBlockY(), pasteLoc.getBlockZ());
				if (centered) {
					int width = localSession.getClipboard().getWidth() / 2;
					int length = localSession.getClipboard().getLength() / 2;
					pasteLocation = pasteLocation.subtract(width, 0, length);
				}
			}
			/*
			 * Don't paste air blocks (increases speed, reduces block count) and paste entities in case we have any saved in the schematic.
			 */
			localSession.getClipboard().paste(editSession, pasteLocation, ignoreAirBlocks, pasteEntities);
			editSession.flushQueue();
			worldEdit.flushBlockBag(null, editSession);
			return true;
		} catch (FilenameException e) {
			Logger.getLogger("WorldEditAPI").log(Level.WARNING, String.format("WorldEditAPI pasteSchematic() Error: Couldn't find the specified schematic file %s.", schematic.getName()));
			if (printStackTraces) {
				e.printStackTrace();
			}
		} catch (IOException | DataException e) {
			Logger.getLogger("WorldEditAPI").log(Level.WARNING, String.format("WorldEditAPI pasteSchematic() Error: Couldn't read from specified schematic file %s.", schematic.getName()));
			if (printStackTraces) {
				e.printStackTrace();
			}
		} catch (MaxChangedBlocksException | EmptyClipboardException e) {
			Logger.getLogger("WorldEditAPI").log(Level.WARNING, String.format("WorldEditAPI pasteSchematic() Error: Schematic file %s is empty or has too many blocks. Check WorldEdit max changed blocks settings.", schematic.getName()));
			if (printStackTraces) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Sets up paste settings that will take effect on all subsequent pastes. By default both settings are false. 
	 * 
	 * @param ignoreAirBlocks True to ignore (not paste) air blocks. Helps with speed and maximum blocks limit
	 * @param pasteEntities True to paste any entities saved in the schematic
	 */
	public void setPasteAttrib(boolean ignoreAirBlocks, boolean pasteEntities) {
		this.ignoreAirBlocks = ignoreAirBlocks;
		this.pasteEntities = pasteEntities;
	}
	
	private static final String EXTENSION = "schematic";
	private static WorldEditPlugin plugin = null;
	private static WorldEdit worldEdit = null;
	private static boolean printStackTraces = false;
	
	private boolean ignoreAirBlocks = false;
	private boolean pasteEntities = false;
	private final LocalSession localSession;
	private final EditSession editSession;
}
