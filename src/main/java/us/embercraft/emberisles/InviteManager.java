package us.embercraft.emberisles;

import java.util.UUID;

import us.embercraft.emberisles.datatypes.Island;

public class InviteManager {
	public enum InviteType {
		ISLAND_ADD_MEMBER,
		ISLAND_ADD_HELPER;
	}
	
	public class Invite {
		private InviteType type;
		private UUID sender;
		private UUID recipient;
		private Island island;
		private long helperDuration;
		private long inviteExpire;
	}
}
