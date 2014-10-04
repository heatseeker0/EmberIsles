package us.embercraft.emberisles;

import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import us.embercraft.emberisles.datatypes.Island;
import us.embercraft.emberisles.datatypes.IslandProtectionAccessLevel;
import us.embercraft.emberisles.datatypes.IslandProtectionFlag;
import us.embercraft.emberisles.datatypes.WorldSettings;
import us.embercraft.emberisles.datatypes.WorldType;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.SLAPI;

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

		if (getServer().getServicesManager().getRegistration(Economy.class) != null) {
			this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
		} else {
			logErrorMessage("No economy plugin detected. Disabling plugin.");
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
			getWorldManager().setDefaultWorldSettings(type, settings);
		}
		
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
			SLAPI.save(getWorldManager().getAll(type), getDataFolder() + "/" + String.format(ISLANDS_FILE_TEMPLATE, type.getConfigKey()));
		} catch(Exception e) {
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
            	getWorldManager().addAll(type, (Set<Island>) SLAPI.load(dataFolder + "/" + String.format(ISLANDS_FILE_TEMPLATE, type.getConfigKey())));
            }
            catch(Exception e) {
                logErrorMessage(String.format("Critical error while loading ISLAND data from disk for world %s. Error message: %s", type.getConfigKey(), e.getMessage()));
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
	
	public BitSet getDefaultProtectionFlags(final IslandProtectionAccessLevel accessLevel) {
		return defaultProtectionFlags.get(accessLevel);
	}
	
	public String getWorldGenerator() {
		return worldGenerator;
	}
	
	/**
	 * Called automatically by the event handlers on player login.
	 * @param player Player that logged in.
	 */
	public void onPlayerJoin(Player player) {
		getPlayerManager().addPlayer(player.getUniqueId(), player.getName());
		//TODO: Update island owner last login time if this player owns or is member of an island.
	}
	
	/*
	 * Misc internally used variables.
	 */
	private static EmberIsles instance;
	public static final String EOL = System.getProperty("line.separator");
	public Economy economy;
    private static Logger logger = Logger.getLogger("Minecraft.EmberIsles");
    private PluginManager pluginManager;
    final static int TICKS_PER_MINUTE = 60 * 20;
	
	/*
	 * Config settings
	 */
	public static FileConfiguration config;
	private final Map<String, String> messages = new HashMap<>();
	private final Map<IslandProtectionAccessLevel, BitSet> defaultProtectionFlags = new HashMap<>();
	private String worldGenerator;
	
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
