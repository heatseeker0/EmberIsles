package us.embercraft.emberisles;

import java.util.UUID;

import us.embercraft.emberisles.datatypes.Island;

public class InviteManager {
	public enum InviteType {
		ISLAND_ADD_MEMBER,
		ISLAND_ADD_HELPER;
	}
	
	public class Invite {
		public Invite(InviteType type, UUID sender, UUID recipient, Island island) {
			this(type, sender, recipient, island, 0);
		}
		
		public Invite(InviteType type, UUID sender, UUID recipient, Island island, long helperDuration) {
			this.type = type;
			this.sender = sender;
			this.recipient = recipient;
			this.island = island;
			this.helperDuration = helperDuration;
		}
		
		private InviteType type;
		private UUID sender;
		private UUID recipient;
		private Island island;
		private long helperDuration;
		private long inviteExpire;
	}
}
