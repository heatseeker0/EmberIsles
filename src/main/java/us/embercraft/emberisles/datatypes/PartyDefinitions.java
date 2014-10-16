package us.embercraft.emberisles.datatypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

public class PartyDefinitions {
	public PartyDefinitions() {
		// empty
	}
	
	/**
	 * How long after a member invite has been sent it's considered expired and a new one can be sent.
	 * @param memberInviteExpire Member expire time in milliseconds
	 */
	public void setMemberInviteExpire(final long memberInviteExpire) {
		this.memberInviteExpire = memberInviteExpire;
	}
	
	/**
	 * How long after a member invite has been sent it's considered expired and a new one can be sent.
	 * @return Expiration time in milliseconds
	 */
	public long getMemberInviteExpire() {
		return memberInviteExpire;
	}
	
	/**
	 * How long after a helper invite has been sent it's considered expired and a new one can be sent.
	 * helperInviteExpire Helper expire time in milliseconds
	 */
	public void setHelperInviteExpire(final long helperInviteExpire) {
		this.helperInviteExpire = helperInviteExpire;
	}
	
	/**
	 * How long after a helper invite has been sent it's considered expired and a new one can be sent.
	 * @return Expiration time in milliseconds
	 */
	public long getHelperInviteExpire() {
		return helperInviteExpire;
	}
	
	/**
	 * Adds a new party rank.
	 * @param permissionNode Permission node the player must have to qualify for this
	 * @param maxPartyMembers How many members can the party have if player has this permission node
	 */
	public void addPartyRank(final String permissionNode, final int maxPartyMembers) {
		partyLimits.put(permissionNode, maxPartyMembers);
	}
	
	/**
	 * Returns the maximum number of members this player party can have, based on player rank. 
	 * @param player
	 * @return
	 */
	public int getMaxPartyLimit(final Player player) {
		if (player == null) {
			return 0;
		}
		// Integer to avoid boxing - unboxing
		Integer result = 0;
		for (Entry<String, Integer> entry : partyLimits.entrySet()) {
			if (player.hasPermission(entry.getKey()) && entry.getValue().compareTo(result) > 0) {
				result = entry.getValue();
			}
		}
		
		return result;
	}
	
	/**
	 * How long to add a helper to an island.
	 * @return Helper duration time in milliseconds
	 */
	public long getHelperDefaultDuration() {
		return helperDefaultDuration;
	}

	/**
	 * Sets for how long to add a helper to an island.
	 * helperDefaultDuration Helper duration time in milliseconds
	 */
	public void setHelperDefaultDuration(long helperDefaultDuration) {
		this.helperDefaultDuration = helperDefaultDuration;
	}

	private Map<String, Integer> partyLimits = new HashMap<>();
	private long memberInviteExpire;
	private long helperInviteExpire;
	private long helperDefaultDuration;
}
