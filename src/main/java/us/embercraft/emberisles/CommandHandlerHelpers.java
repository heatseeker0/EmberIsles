package us.embercraft.emberisles;

import us.embercraft.emberisles.datatypes.WorldType;

/**
 * Collection of validators and converters for commonly used command line parameters.
 * 
 * @author Catalin Ionescu
 *
 */
public class CommandHandlerHelpers {
	/**
	 * Transforms a given world type given as string into corresponding WorldType if it exists or null for invalid strings.
	 * @param worldName World type given as string
	 * @return WorldType if it exists or null for invalid strings
	 */
	public static WorldType worldNameToType(String worldName) {
		if (worldName == null) {
			return null;
		}
		try {
			WorldType type = WorldType.getEnum(worldName.toLowerCase());
			return type;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
