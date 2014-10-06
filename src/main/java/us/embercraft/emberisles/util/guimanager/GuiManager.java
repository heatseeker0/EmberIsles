package us.embercraft.emberisles.util.guimanager;

import java.awt.IllegalComponentStateException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiManager {
	private GuiManager() {
		// empty
	}
	
	public static GuiManager getInstance() {
		if (instance == null)
			instance = new GuiManager();
		return instance;
	}
	
	/**
	 * Initializes the Gui manager internal data structures and sets up the event listeners associated
	 * with gui interaction. <strong>Must</strong> be called before any Gui screens are set up or used.
	 * 
	 * <p>Typically this should be called in plugin onEnable().</p>
	 * 
	 * @param plugin Plugin this Gui manager is ran from.
	 */
	@SuppressWarnings("hiding")
	public void initManager(JavaPlugin plugin) {
		this.plugin = plugin;
		pluginName = plugin.getDescription().getName();
		plugin.getServer().getPluginManager().registerEvents(new GuiListener(plugin), plugin);
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Called internally by {@link GuiListener} to determine if a click has been made in one of our
	 * custom chest GUIs. Calls {@link AbstractGui#handleMenuSelection(player, menuTitle, menuItem)}
	 * if the click was made in one of our menus.
	 * 
	 * <p>The comparison and decision is made based solely on the menu title. Hence all menu titles
	 * in the plugin must be unique and not match Vanilla menu titles or titles made by other
	 * running plugins on the same server.</p>
	 * 
	 * @param player Player that made the click
	 * @param menuTitle Title of the chest the click was made in
	 * @param menuItem Item the player clicked on
	 * @return AbstractGui for the menu player clicked in or null if it wasn't one of our menus.
	 */
	public AbstractGui playerMenuSelect(final Player player, final String menuTitle, final ItemStack menuItem) {
		if (plugin == null)
			throw new IllegalComponentStateException("Manager not fully initialized (plugin is null).");
		
		if (player == null || menuTitle == null || menuItem == null)
			return null;
		
		for (AbstractGui gui : guiList) {
			if (gui.getTitle().equals(menuTitle)) {
				gui.handleMenuSelection(player, menuTitle, menuItem);
				return gui;
			}
		}
		return null;
	}
	
	/**
	 * Retrieves the {@link AbstractGui} associated with the specified config key or null if none was found.
	 * @param key Config key <strong>(not menu title!)</strong> to retrieve menu for
	 * @return AbstractGui associated with the specified key or null if none was found.
	 */
	public AbstractGui getGuiByKey(final String key) {
		if (plugin == null)
			throw new IllegalComponentStateException("Manager not fully initialized (plugin is null).");
		
		for (AbstractGui gui : guiList) {
			if (gui.getConfigKey().equals(key)) {
				return gui;
			}
		}
		return null;
	}
	
	/**
	 * Adds a new gui to this manager.
	 * @param gui Gui to be added
	 */
	public void addNewGui(AbstractGui gui) {
		guiList.add(gui);
	}
	
	/**
	 * Removes all guis from this manager.
	 */
	public void clear() {
		guiList.clear();
	}
	
	public static void logErrorMessage(final String msg) {
		logger.severe(String.format("[%s] %s", pluginName, msg));
	}
	
	public static void logInfoMessage(final String msg) {
		logger.info(String.format("[%s] %s", pluginName, msg));
	}
	
	private static GuiManager instance = null;
	private static String pluginName;
	private static Logger logger = Logger.getLogger("GuiManager");

	private JavaPlugin plugin = null;
    Set<AbstractGui> guiList = new HashSet<>();
}
