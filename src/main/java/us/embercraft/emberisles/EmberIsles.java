package us.embercraft.emberisles;

import java.io.File;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import us.embercraft.emberisles.datatypes.FutureMenuCommand;
import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandLookupKey;
import us.embercraft.emberisles.datatypes.IslandProtectionAccessLevel;
import us.embercraft.emberisles.datatypes.IslandProtectionFlag;
import us.embercraft.emberisles.datatypes.SchematicDefinition;
import us.embercraft.emberisles.datatypes.WorldSettings;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.thirdparty.VaultAPI;
import us.embercraft.emberisles.thirdparty.WorldEditAPI;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.SLAPI;
import us.embercraft.emberisles.util.guimanager.GuiManager;

public class EmberIsles extends JavaPlugin {
	public class AutoSavePlayers implements Runnable {
	    @Override
		public void run() {
	    	if (getPlayerManager().isDirty()) {
		        saveDatFilesPlayer();
		        getPlayerManager().clearDirty();
		        logInfoMessage("Player files auto-saved.");
	    	}
	    }
	}
	
	public class AutoSaveWorlds implements Runnable {
		@Override
		public void run() {
			for (WorldType type : WorldType.values()) {
				if (getWorldManager().isDirty(type)) {
					saveDatFilesWorld(type);
					getWorldManager().clearDirty(type);
					logInfoMessage(String.format("World files for world %s auto-saved.", type.getConfigKey()));
				}
			}
		}
	}
	
	@Override
	public void onEnable() {
		instance = this;
		pluginManager = getServer().getPluginManager();

		if (!VaultAPI.initAPI()) {
			logErrorMessage("No economy plugin detected. Disabling plugin.");
			pluginManager.disablePlugin(this);
    		return;
		}
		
		// TODO: Set printStackTraces to false for production use.
		if (!WorldEditAPI.initAPI(true)) {
			logErrorMessage("WorldEdit couldn't be found or is disabled. Disabling plugin.");
			pluginManager.disablePlugin(this);
			return;
		}
		
		logInfoMessage("Loading data structures:");
		logInfoMessage("** Player data");
		if (!loadDatFilesPlayer()) {
			logErrorMessage("Plugin disabled.");
			pluginManager.disablePlugin(this);
    		return;
		}
		logInfoMessage("** World data");
		for (WorldType type : WorldType.values()) {
			logInfoMessage(String.format("**** %s", type.getConfigKey()));
			if (!loadDatFilesWorld(type)) {
				logErrorMessage("Plugin disabled.");
				pluginManager.disablePlugin(this);
				return;
			}
		}
		logInfoMessage("[All loading done]");
		
		/*
		 * The Gui manager initialization should be finalized before invoking applyConfig() in case any
		 * Gui screens are generated there.
		 */
		getGuiManager().initManager(this);
		
		saveDefaultConfig();
		applyConfig();
		
		logInfoMessage("Loading Bukkit island worlds");
		for (WorldType type : WorldType.values()) {
			logInfoMessage(String.format("** %s", type.getConfigKey()));
			if (!getWorldManager().generateBukkitWorld(type)) {
				logErrorMessage(String.format("Fatal error while loading or generating world %s. Plugin disabled.", type.getConfigKey()));
				pluginManager.disablePlugin(this);
				return;
			}
		}
		logInfoMessage("[All loading done]");
		
		pluginManager.registerEvents(new PlayerLoginListener(this), this);
		pluginManager.registerEvents(new IslandProtectionListener(this), this);
		
		getCommand("island").setExecutor(new IslandCommandHandler(this));
		getCommand("islandev").setExecutor(new IslandevCommandHandler(this));
	}
	
	@Override
	public void onDisable() {
		logInfoMessage("Saving data structures:");
		logInfoMessage("** Player data");
		saveDatFilesPlayer();
		logInfoMessage("** World data");
		for (WorldType type : WorldType.values()) {
			logInfoMessage(String.format("**** %s", type.getConfigKey()));
			saveDatFilesWorld(type);
		}
		logInfoMessage("[All saving done]");
	}
	
	public void applyConfig() {
		reloadConfig();
		config = getConfig();
		
		messages.clear();
		for (String msgKey : config.getConfigurationSection("messages").getKeys(false)) {
			messages.put(msgKey, MessageUtils.parseColors(config.getString("messages." + msgKey)));
		}
		
		worldGenerator = config.getString("world-generator", "CleanroomGenerator:.");
		
		for (IslandProtectionAccessLevel accessLevel : IslandProtectionAccessLevel.values()) {
			BitSet bits = new BitSet();
			for (IslandProtectionFlag flag : IslandProtectionFlag.values()) {
				bits.set(flag.id(), config.getBoolean(String.format("island-protection-defaults.%s.%s", accessLevel.getConfigKey(), flag.getConfigKey()), false));
			}
			defaultProtectionFlags.put(accessLevel, bits);
		}
		
		for (WorldType type : WorldType.values()) {
			WorldSettings settings = new WorldSettings();
			settings.setBukkitWorldName(config.getString(String.format("world-settings.%s.bukkit-name", type.getConfigKey())));
			settings.setIslandSize(config.getInt(String.format("world-settings.%s.island-size", type.getConfigKey()), settings.getIslandSize()));
			settings.setBorderSize(config.getInt(String.format("world-settings.%s.border-size", type.getConfigKey()), settings.getBorderSize()));
			settings.setY(config.getInt(String.format("world-settings.%s.y", type.getConfigKey()), settings.getY()));
			try {
				settings.setStartingBiome(Biome.valueOf(config.getString(String.format("world-settings.%s.starting-biome", type.getConfigKey()),
						settings.getStartingBiome().toString()).toUpperCase()));
			} catch (IllegalArgumentException e) {
				logErrorMessage(String.format("Wrong biome type in config.yml for key world-settings.%s.starting-biome. Using PLAINS for this world type.", type.getConfigKey()));
			}
			settings.setAllowParty(config.getBoolean(String.format("world-settings.%s.allow-party", type.getConfigKey()), settings.getAllowParty()));
			settings.setIslandsPerRow(config.getInt(String.format("world-settings.%s.islands-per-row", type.getConfigKey()), settings.getIslandsPerRow()));
			getWorldManager().setDefaultWorldSettings(type, settings);
		}
		
		final File dataFolder = getDataFolder();
		
		for (WorldType type : WorldType.values()) {
			getWorldManager().clearSchematicDefinitions(type);
			for (String schemKey : config.getConfigurationSection(String.format("schematics.%s", type.getConfigKey())).getKeys(false)) {
				if (!config.contains(String.format("schematics.%s.%s.permission", type.getConfigKey(), schemKey)) ||
						!config.contains(config.getString(String.format("schematics.%s.%s.file", type.getConfigKey(), schemKey)))) {
					logErrorMessage(String.format("One or more mandatory settings for schematic definition schematics.%s.%s is missing. This schematic will be ignored until this error is fixed.", type.getConfigKey(), schemKey));
					continue;
				}
				File schemFile = new File(dataFolder, config.getString(String.format("schematics.%s.%s.file", type.getConfigKey(), schemKey)));
				if (!schemFile.exists()) {
					logErrorMessage(String.format("The specified file for schematic schematics.%s.%s is missing. This schematic will be ignored until this error is fixed.", type.getConfigKey(), schemKey));
					continue;
				}
				Material material = Material.getMaterial(config.getString(String.format("schematics.%s.%s.icon", type.getConfigKey(), schemKey)));
				if (material == null) {
					logErrorMessage(String.format("Invalid icon for schematic schematics.%s.%s. This schematic will be ignored until this error is fixed.", type.getConfigKey(), schemKey));
					continue;
				}
				SchematicDefinition definition = new SchematicDefinition(type,
						material,
						(short) config.getInt(String.format("schematics.%s.%s.durability", type.getConfigKey(), schemKey)),
						MessageUtils.parseColors(config.getString(String.format("schematics.%s.%s.title", type.getConfigKey(), schemKey), "Undefined")),
						config.getString(String.format("schematics.%s.%s.permission", type.getConfigKey(), schemKey)),
						schemFile,
						MessageUtils.parseColors(config.getStringList(String.format("schematics.%s.%s.lore", type.getConfigKey(), schemKey))),
						MessageUtils.parseColors(config.getStringList(String.format("schematics.%s.%s.noperm-lore", type.getConfigKey(), schemKey))));
				getWorldManager().addSchematicDefinition(type, definition);
			}
		}
		
		setupAutomaticAllocators();
		
		//TODO: Set up the gui screens
		
		if (playersAutoSaveTaskId > 0) {
			Bukkit.getScheduler().cancelTask(playersAutoSaveTaskId);
			playersAutoSaveTaskId = -1;
		}
        int autoSave = config.getInt("player-auto-save", 17);
        if (autoSave > 0)
        	playersAutoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new AutoSavePlayers(), autoSave * TICKS_PER_MINUTE, autoSave * TICKS_PER_MINUTE);
        
        if (worldAutoSaveTaskId > 0) {
        	Bukkit.getScheduler().cancelTask(worldAutoSaveTaskId);
        	worldAutoSaveTaskId = -1;
        }
        autoSave = config.getInt("world-auto-save", 11);
        if (autoSave > 0)
        	worldAutoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new AutoSaveWorlds(), autoSave * TICKS_PER_MINUTE, autoSave * TICKS_PER_MINUTE);
	}
	
	private void setupAutomaticAllocators() {
		logInfoMessage("Setting up automatic island allocators...");
		for (WorldType type : WorldType.values()) {
			getWorldManager().initializeAllocator(type);
		}
		logInfoMessage("[Allocators set up]");
	}
	
    /**
     * Saves the player -> uuid maps to disk.
     */
    protected void saveDatFilesPlayer() {
        try {
            SLAPI.save(getPlayerManager().getAll(), getDataFolder() + "/" + PLAYERS_FILE);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the player -> uuid maps from disk.
     * @return Returns true if the maps were successfully loaded, false on error.
     */
	protected boolean loadDatFilesPlayer() {
    	final File dataFolder = getDataFolder();
    	
        if (getPlayerManager().isEmpty() && (new File(dataFolder, PLAYERS_FILE)).exists()) {
            try {
                getPlayerManager().addAll((Map<UUID, String>) SLAPI.load(dataFolder + "/" + PLAYERS_FILE));
            }
            catch(Exception e) {
                logErrorMessage(String.format("Critical error while loading PLAYER data from disk. Error message: %s", e.getMessage()));
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
	
	/**
	 * Saves the island maps to disk.
	 * @param type World type
	 */
	protected void saveDatFilesWorld(WorldType type) {
		try {
			SLAPI.save(getWorldManager().getAllOccupied(type), getDataFolder() + "/" + String.format(ISLANDS_FILE_TEMPLATE, type.getConfigKey()));
		} catch(Exception e) {
            e.printStackTrace();
        }
		try {
			SLAPI.save(getWorldManager().getAllFree(type), getDataFolder() + "/" + String.format(FREE_ISLANDS_FILE_TEMPLATE, type.getConfigKey()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the island world data from disk.
	 * @param type World type
	 * @return Returns true if the maps were successfully loaded, false on error.
	 */
	protected boolean loadDatFilesWorld(WorldType type) {
    	final File dataFolder = getDataFolder();
    	
        if (getWorldManager().isEmpty(type) && (new File(dataFolder, String.format(ISLANDS_FILE_TEMPLATE, type.getConfigKey()))).exists()) {
            try {
            	getWorldManager().addAllOccupied(type, (Collection<Island>) SLAPI.load(dataFolder + "/" + String.format(ISLANDS_FILE_TEMPLATE, type.getConfigKey())));
            }
            catch(Exception e) {
                logErrorMessage(String.format("Critical error while loading occupied ISLAND data from disk for world %s. Error message: %s", type.getConfigKey(), e.getMessage()));
                e.printStackTrace();
                return false;
            }
        }
        
        if (getWorldManager().isEmpty(type) && (new File(dataFolder, String.format(FREE_ISLANDS_FILE_TEMPLATE, type.getConfigKey()))).exists()) {
            try {
            	getWorldManager().addAllFree(type, (Collection<IslandLookupKey>) SLAPI.load(dataFolder + "/" + String.format(FREE_ISLANDS_FILE_TEMPLATE, type.getConfigKey())));
            }
            catch(Exception e) {
                logErrorMessage(String.format("Critical error while loading free ISLAND data from disk for world %s. Error message: %s", type.getConfigKey(), e.getMessage()));
                e.printStackTrace();
                return false;
            }
        }
        
        return true;
	}
	
	public String getMessage(final String key) {
		if (messages.containsKey(key)) {
			return messages.get(key);
		}
		final String errorMsg = "No message text in config.yml for " + key; 
		logErrorMessage(errorMsg);
		return errorMsg;
	}
	
	public void logInfoMessage(final String msg) {
		logger.info(String.format("[%s] %s", getDescription().getName(), msg));
	}
	
	public void logErrorMessage(final String msg) {
		logger.severe(String.format("[%s] %s", getDescription().getName(), msg));
	}
	
	public static EmberIsles getInstance() {
		return instance;
	}
	
	public PlayerManager getPlayerManager() {
		return playerManager;
	}
	
	public WorldManager getWorldManager() {
		return worldManager;
	}
	
	public GuiManager getGuiManager() {
		return guiManager;
	}
	
	public BitSet getDefaultProtectionFlags(final IslandProtectionAccessLevel accessLevel) {
		return defaultProtectionFlags.get(accessLevel);
	}
	
	public String getWorldGenerator() {
		return worldGenerator;
	}
	
	public void addFutureCommand(final UUID playerId, final FutureMenuCommand cmd) {
		futureCommands.put(playerId, cmd);
	}
	
	public FutureMenuCommand getFutureCommand(final UUID playerId) {
		return futureCommands.get(playerId);
	}
	
	public void deleteFutureCommand(final UUID playerId) {
		futureCommands.remove(playerId);
	}
	
	/**
	 * Called automatically by the event handlers on player login.
	 * @param player Player that logged in.
	 */
	public void onPlayerJoin(Player player) {
		getPlayerManager().addPlayer(player.getUniqueId(), player.getName());
		/*
		 * Update last island activity for all worlds for this player.
		 */
		final long currentTime = System.currentTimeMillis();
		for (WorldType type : WorldType.values()) {
			Island island = getWorldManager().getPlayerIsland(type, player.getUniqueId());
			if (island != null) {
				island.setOwnerLoginTime(currentTime);
				getWorldManager().setDirty(type);
			}
		}
	}
	
	/**
	 * Runs validity checks (i.e. player doesn't already own an island) and creates a new island for
	 * given player.
	 * 
	 * @param player Player to set as owner for the island
	 * @param cmd {@link FutureMenuCommand} island creation command
	 */
	public void createIsland(Player player, FutureMenuCommand cmd) {
		if (getWorldManager().getPlayerIsland(cmd.getWorldType(), player.getUniqueId()) != null) {
			player.sendMessage(getMessage("error-already-island"));
			return;
		}
		IslandLookupKey key = getWorldManager().getNextFreeIslandLocation(cmd.getWorldType());
		if (key == null) {
			logErrorMessage(String.format("There are no more new islands available in world %s for new players.", cmd.getWorldType().getConfigKey()));
			player.sendMessage(String.format(getMessage("error-no-free-islands"), cmd.getWorldType().getConfigKey()));
			return;
		}
		Island island = new Island(key);
		island.setOwner(player.getUniqueId());
		island.setOwnerLoginTime(System.currentTimeMillis());
		getWorldManager().addIsland(cmd.getWorldType(), island);
		
		// TODO: Paste the schematic, teleport player, etc.
	}
	
	/*
	 * Misc internally used variables.
	 */
	private static EmberIsles instance;
	public static final String EOL = System.getProperty("line.separator");
    private static Logger logger = Logger.getLogger("Minecraft.EmberIsles");
    private PluginManager pluginManager;
    final static int TICKS_PER_MINUTE = 60 * 20;
    private Map<UUID, FutureMenuCommand> futureCommands = new HashMap<>();
	
	/*
	 * Config settings
	 */
	public static FileConfiguration config;
	private final Map<String, String> messages = new HashMap<>();
	private final Map<IslandProtectionAccessLevel, BitSet> defaultProtectionFlags = new HashMap<>();
	private String worldGenerator;
	private static GuiManager guiManager = GuiManager.getInstance();
	
	/*
	 * Data store
	 */
	private int playersAutoSaveTaskId = -1;
	public static final String PLAYERS_FILE = "players.dat";
	private static PlayerManager playerManager = PlayerManager.getInstance();
	
	private int worldAutoSaveTaskId = -1;
	public static final String ISLANDS_FILE_TEMPLATE = "islands_%s.dat";
	public static final String FREE_ISLANDS_FILE_TEMPLATE = "freeIslands_%s.dat";
	private static WorldManager worldManager = WorldManager.getInstance();
}
