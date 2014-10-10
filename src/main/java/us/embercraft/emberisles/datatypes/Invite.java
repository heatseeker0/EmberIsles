package us.embercraft.emberisles.datatypes;

import java.io.Serializable;
import java.util.UUID;

import us.embercraft.emberisles.EmberIsles;

public class Invite implements Serializable {
	public Invite(WorldType worldType, InviteType inviteType, UUID sender, UUID recipient) {
		this(worldType, inviteType, sender, recipient, 0);
	}
	
	public Invite(WorldType worldType, InviteType inviteType, UUID sender, UUID recipient, long helperDuration) {
		this.worldType = worldType;
		this.inviteType = inviteType;
		this.sender = sender;
		this.recipient = recipient;
		this.helperDuration = helperDuration;
		switch (inviteType) {
			case ISLAND_ADD_MEMBER:
				this.inviteExpire = System.currentTimeMillis() + EmberIsles.getInstance().getPartyDefinitions().getMemberInviteExpire();
				break;
			case ISLAND_ADD_HELPER:
				this.inviteExpire = System.currentTimeMillis() + EmberIsles.getInstance().getPartyDefinitions().getHelperInviteExpire();
				break;
		}
	}
	
	public WorldType getWorldType() {
		return worldType;
	}
	
	public InviteType getInviteType() {
		return inviteType;
	}
	
	public UUID getSender() {
		return sender;
	}
	
	public UUID getRecipient() {
		return recipient;
	}
	
	public boolean isExpired(final long currentTime) {
		return inviteExpire >= currentTime;
	}
	
	public long getHelperDuration() {
		return helperDuration;
	}
	
	private static final long serialVersionUID = 1L;
	
	private InviteType inviteType;
	private WorldType worldType;
	private UUID sender;
	private UUID recipient;
	private long helperDuration;
	private long inviteExpire;
}