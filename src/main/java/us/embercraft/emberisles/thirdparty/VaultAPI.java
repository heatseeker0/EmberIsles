package us.embercraft.emberisles.thirdparty;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultAPI {
	/**
	 * Initializes the link to Vault. Returns true on success. Method should be called once per plugin, before
	 * creating any instances of this class.
	 * @return True on success
	 */
	public static boolean initAPI() {
		if (Bukkit.getServer().getServicesManager().getRegistration(Economy.class) != null) {
			economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
		}
		return isInitialized();
	}
	
	/**
	 * Returns true if the VaultAPI is successfully set up and ready for use.
	 * @return True if the VaultAPI is ready for use.
	 */
	public static boolean isInitialized() {
		return economy != null;
	}
	
	/**
	 * Pays specified amount to a player. The amount MUST be a positive number or <i>IllegalArgumentException</i> is thrown.
	 * @param player Player to pay to
	 * @param amount <i>Positive</i> amount to pay
	 * @return True on success
	 */
	public static boolean pay(final Player player, final double amount) {
		if (!isInitialized() && !initAPI()) {
			throw new IllegalStateException("Couldn't initialize VaultAPI");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount should be a positive number");
		}
		return economy.depositPlayer(player, amount).type == ResponseType.SUCCESS;
	}
	
	/**
	 * Withdraws specified amount from a player balance. The amount MUST be a positive number or <i>IllegalArgumentException</i> is thrown.
	 * @param player Player to withdraw from
	 * @param amount <i>Positive</i> amount to withdraw
	 * @return True on success
	 */
	public static boolean withdraw(final Player player, final double amount) {
		if (!isInitialized() && !initAPI()) {
			throw new IllegalStateException("Couldn't initialize VaultAPI");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("Amount should be a positive number");
		}
		return economy.withdrawPlayer(player, amount).type == ResponseType.SUCCESS;
	}
	
	/**
	 * Returns the specified player current balance.
	 * @param player Player to get the balance for
	 * @return Amount currently held in player balance
	 */
	public static double getBalance(final Player player) {
		if (!isInitialized() && !initAPI()) {
			throw new IllegalStateException("Couldn't initialize VaultAPI");
		}
		return economy.getBalance(player);
	}
	
	private static Economy economy = null;
}
