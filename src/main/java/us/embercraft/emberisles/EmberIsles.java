package us.embercraft.emberisles;

import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import us.embercraft.emberisles.datatypes.IslandProtectionAccessLevel;
import us.embercraft.emberisles.datatypes.IslandProtectionFlag;
import us.embercraft.emberisles.util.MessageUtils;
import us.embercraft.emberisles.util.SLAPI;

public class EmberIsles extends JavaPlugin {
	public class AutoSavePlayers implements Runnable {
	    @Override
		public void run() {
	        saveDatFilesPlayer();
	        logInfoMessage("Player files auto-saved.");
	    }
	}
	
	@Override
	public void onEnable() {
		instance = this;
		if (getServer().getServicesManager().getRegistration(Economy.class) != null) {
			this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
		} else {
			logErrorMessage("No economy plugin detected. Disabling plugin.");
			Bukkit.getPluginManager().disablePlugin(this);
    		return;
		}
		
		logInfoMessage("Loading data structures...");
		if (!loadDatFilesPlayer()) {
			logErrorMessage("Plugin disabled.");
			Bukkit.getPluginManager().disablePlugin(this);
    		return;
		}
		logInfoMessage("[done]");
		
		saveDefaultConfig();
		applyConfig();
		
		pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new PlayerLoginListener(this), this);
		pluginManager.registerEvents(new IslandProtectionListener(this), this);
		
		getCommand("island").setExecutor(new IslandCommandHandler(this));
		getCommand("islandev").setExecutor(new IslandevCommandHandler(this));
	}
	
	@Override
	public void onDisable() {
		logInfoMessage("Saving data structures...");
		saveDatFilesPlayer();
		logInfoMessage("[done]");
	}
	
	public void applyConfig() {
		reloadConfig();
		config = getConfig();
		
		messages.clear();
		for (String msgKey : config.getConfigurationSection("messages").getKeys(false)) {
			messages.put(msgKey, MessageUtils.parseColors(config.getString("messages." + msgKey)));
		}
		
		for (IslandProtectionAccessLevel accessLevel : IslandProtectionAccessLevel.values()) {
			BitSet bits = new BitSet();
			for (IslandProtectionFlag flag : IslandProtectionFlag.values()) {
				bits.set(flag.id(), config.getBoolean(String.format("island-protection-defaults.%s.%s", accessLevel.getConfigKey(), flag.getConfigKey()), false));
			}
			defaultProtectionFlags.put(accessLevel, bits);
		}
		
		if (playersAutoSaveTaskId > 0)
			Bukkit.getScheduler().cancelTask(playersAutoSaveTaskId);
        final int autoSave = getConfig().getInt("player-auto-save", 34);
        if (autoSave > 0)
        	playersAutoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new AutoSavePlayers(), autoSave * 1200, autoSave * 1200);
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
                logErrorMessage(String.format("Critical error while loading player data from disk. Error message: %s. Full stack trace:", e.getMessage()));
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
	
	public BitSet getDefaultProtectionFlags(final IslandProtectionAccessLevel accessLevel) {
		return defaultProtectionFlags.get(accessLevel);
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
	
	/*
	 * Config settings
	 */
	public static FileConfiguration config;
	private final Map<String, String> messages = new HashMap<>();
	private final Map<IslandProtectionAccessLevel, BitSet> defaultProtectionFlags = new HashMap<>();
	
	/*
	 * Data store
	 */
	private int playersAutoSaveTaskId = -1;
	public static final String PLAYERS_FILE = "players.dat";
	private static PlayerManager playerManager = PlayerManager.getInstance();
}
