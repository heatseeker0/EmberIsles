package us.embercraft.emberisles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import us.embercraft.emberisles.datatypes.TimestampString;
import us.embercraft.emberisles.util.RandomStringGenerator;
import us.embercraft.emberisles.util.RandomStringGenerator.Mode;

public class ConfirmCodeManager {
    private class CodeExpirer implements Runnable {
        @Override
        public void run() {
            Iterator<Entry<Player, TimestampString>> iter = codes.entrySet().iterator();
            final long currentTime = System.currentTimeMillis();
            while (iter.hasNext()) {
                Entry<Player, TimestampString> entry = iter.next();
                if (entry.getValue().getTimeStamp() <= currentTime) {
                    if (entry.getKey().isOnline()) {
                        entry.getKey().sendMessage(EmberIsles.getInstance().getMessage("confirm-code-expired"));
                    }
                    iter.remove();
                }
            }
        }
    }

    private ConfirmCodeManager() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(EmberIsles.getInstance(), new CodeExpirer(), 20L, 20L);
    }

    public static ConfirmCodeManager getInstance() {
        if (instance == null)
            instance = new ConfirmCodeManager();
        return instance;
    }

    /**
     * Sets the clear / delete confirmation code expire delay, in seconds.
     * 
     * @param delay Delay in seconds
     */
    public void setCodeExpireTime(int secondsDelay) {
        codeExpireTime = secondsDelay * EmberIsles.MILLISECONDS_PER_SECOND;
    }

    /**
     * Retrieves the clear / delete expire delay.
     * 
     * @return Number of seconds until a confirmation code expires
     */
    public int getCodeExpireTime() {
        return (int) (codeExpireTime / EmberIsles.MILLISECONDS_PER_SECOND);
    }

    /**
     * Generates a new code and associates it with the specified player.
     * 
     * @param player Player to associate with the code
     * @return Newly generated random code
     */
    public String generateCode(final Player player) {
        if (player == null)
            return "";
        final String keyCode = RandomStringGenerator.generateRandomString(4, Mode.ALPHANUMERIC).toLowerCase();
        codes.put(player, new TimestampString(keyCode, System.currentTimeMillis() + codeExpireTime));
        return keyCode;
    }

    /**
     * Checks the given code is valid for specified player.
     * 
     * @param player Player to check
     * @param code Code to check
     * @return True if this code has been issued for specified player and is not expired, false otherwise
     */
    public boolean isValid(final Player player, final String code) {
        return codes.containsKey(player) && codes.get(player).getString().equalsIgnoreCase(code);
    }

    /**
     * Returns the code for specified player if there is a valid one, or empty string if none can be found.
     * 
     * @param player Player to get the code for
     * @return Valid code for specified player or empty string if none can be found
     */
    public String getAndRemove(final Player player) {
        String result = "";
        if (codes.containsKey(player)) {
            result = codes.get(player).getString();
            codes.remove(player);
        }
        return result;
    }

    private static ConfirmCodeManager instance = null;

    private long codeExpireTime = 30 * EmberIsles.MILLISECONDS_PER_SECOND;
    private Map<Player, TimestampString> codes = new HashMap<>();
}
