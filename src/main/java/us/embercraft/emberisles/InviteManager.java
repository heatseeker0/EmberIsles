package us.embercraft.emberisles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import us.embercraft.emberisles.datatypes.Invite;
import us.embercraft.emberisles.datatypes.InviteType;
import us.embercraft.emberisles.datatypes.WorldType;

public class InviteManager {
	/**
	 * Retrieves the full list of invites.
	 * @return
	 */
	public Collection<Invite> getAll() {
		return invites;
	}

	/**
	 * Appends all of the elements in specified collection to the end of the invite list.
	 * @param invites Invites to be added to the list
	 */
	@SuppressWarnings("hiding")
	public void addAll(Collection<Invite> invites) {
		this.invites.addAll(invites);
		dirtyFlag = false;
	}

	/**
	 * Expired invite checker. Typical usage is to call this from a runnable every couple seconds.
	 */
	public void inviteExpirerTick() {
		Iterator<Invite> iter = invites.iterator();
		long currentTime = System.currentTimeMillis();
		
		while (iter.hasNext()) {
			Invite invite = iter.next();
			if (invite.isExpired(currentTime)) {
				iter.remove();
				dirtyFlag = true;
			}
		}
	}
	
	/**
	 * Retrieves a list of all pending player invites.
	 * @param playerId Player UUID to retrieve invites for
	 * @return List of pending player invites
	 */
	public List<Invite> getPlayerInvites(UUID playerId) {
		List<Invite> result = new ArrayList<>();
		if (playerId == null)
			return result;
		
		for (Invite invite : invites) {
			if (invite.getRecipient().equals(playerId)) {
				result.add(invite);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the pending invite sent by a specific player to the recipient or null if none are found.
	 * @param recipientId Recipient of the invite
	 * @param senderId Sender of the invite
	 * @return Invite for matching sender and recipient or null for no match
	 */
	public Invite getPlayerInviteExact(UUID sender, UUID recipient) {
		for (Invite invite : invites) {
			if (invite.getSender().equals(sender) && invite.getRecipient().equals(recipient)) {
				return invite;
			}
		}
		return null;
	}
	
	/**
	 * Removes the specified invite from the pending invites list.
	 * @param invite Invite to remove
	 */
	public void remove(final Invite invite) {
		if (invite == null)
			return;
		
		invites.remove(invite);
		dirtyFlag = true;
	}
	
	/**
	 * Removes all pending invites of a certain type the player has received in the specified world type.
	 * @param worldType World type to search for (normal, challenge, hardcore)
	 * @param inviteType Invite type to search for (member, helper)
	 * @param playerId Player UUID to clear received invites for
	 */
	public void clearPlayerInvites(WorldType worldType, InviteType inviteType, UUID playerId) {
		if (playerId == null)
			return;
		
		Iterator<Invite> iter = invites.iterator();
		while (iter.hasNext()) {
			Invite invite = iter.next();
			if (invite.getRecipient().equals(playerId) && invite.getWorldType() == worldType && invite.getInviteType() == inviteType) {
				iter.remove();
				dirtyFlag = true;
			}
		}
	}
	
	/**
	 * Appends the specified invite to the list of pending invites.
	 * @param invite Invite to be added
	 */
	public void add(final Invite invite) {
		invites.add(invite);
	}
	
	/**
	 * Searches if there's a pending helper invite.
	 * @param sender Sender of the invite
	 * @param recipient Recipient of the invite
	 * @return True if there's a pending helper invite
	 */
	public boolean hasHelperInvite(final UUID sender, final UUID recipient) {
		return hasInvite(sender, recipient, InviteType.ISLAND_ADD_HELPER);
	}
	
	/**
	 * Searches if there's a pending member invite.
	 * @param sender Sender of the invite
	 * @param recipient Recipient of the invite
	 * @return True if there's a pending member invite
	 */
	public boolean hasMemberInvite(final UUID sender, final UUID recipient) {
		return hasInvite(sender, recipient, InviteType.ISLAND_ADD_MEMBER);
	}
	
	/**
	 * Searches if there's a pending invite of specified type.
	 * @param sender Sender of the invite
	 * @param recipient Recipient of the invite
	 * @param type Invite type
	 * @return True if there's a pending invite of specified type
	 */
	public boolean hasInvite(final UUID sender, final UUID recipient, InviteType type) {
		Invite invite = getPlayerInviteExact(sender, recipient);
		return (invite != null && (type == null ? true : invite.getInviteType() == type));
	}
	
	/**
	 * Searches if there's a pending invite of any type.
	 * @param sender Sender of the invite
	 * @param recipient Recipient of the invite
	 * @return True if there's a pending invite of any type
	 */
	public boolean hasInvite(final UUID sender, final UUID recipient) {
		return hasInvite(sender, recipient, null);
	}
	
	public boolean isDirty() {
		return dirtyFlag;
	}
	
	public void setDirty() {
		dirtyFlag = true;
	}
	
	public void clearDirty() {
		dirtyFlag = false;
	}
	
	private boolean dirtyFlag = false;
	private final List<Invite> invites = new LinkedList<>();
}
