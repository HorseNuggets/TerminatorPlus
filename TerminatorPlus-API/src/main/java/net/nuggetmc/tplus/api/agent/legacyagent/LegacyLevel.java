package net.nuggetmc.tplus.api.agent.legacyagent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

public enum LegacyLevel {
    ABOVE(0, 2, 0),
    BELOW(0, -1, 0),
    AT(0, 1, 0),
    AT_D(0, 0, 0),
    NORTH_U(0, 2, -1),
    SOUTH_U(0, 2, 1),
    EAST_U(1, 2, 0),
    WEST_U(-1, 2, 0),
    NORTH(0, 1, -1),
    SOUTH(0, 1, 1),
    EAST(1, 1, 0),
    WEST(-1, 1, 0),
    NORTH_D(0, 0, -1),
    SOUTH_D(0, 0, 1),
    EAST_D(1, 0, 0),
    WEST_D(-1, 0, 0),
    NORTHWEST_D(-1, 0, -1),
    SOUTHWEST_D(-1, 0, 1),
    NORTHEAST_D(1, 0, -1),
    SOUTHEAST_D(1, 0, 1),
    NORTH_D_2(0, -1, -1),
    SOUTH_D_2(0, -1, 1),
    EAST_D_2(1, -1, 0),
    WEST_D_2(-1, -1, 0);
	
	private final int offsetX;
	private final int offsetY;
	private final int offsetZ;
	
	private LegacyLevel(int offsetX, int offsetY, int offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

    private static final Set<LegacyLevel> NON_SIDE = new HashSet<>(Arrays.asList(
        ABOVE,
        BELOW,
        AT,
        AT_D
    ));
    private static final Set<LegacyLevel> SIDE_AT = new HashSet<>(Arrays.asList(
        NORTH,
        SOUTH,
        EAST,
        WEST
    ));
    private static final Set<LegacyLevel> SIDE_UP = new HashSet<>(Arrays.asList(
        NORTH_U,
        SOUTH_U,
        EAST_U,
        WEST_U
    ));
    private static final Set<LegacyLevel> SIDE_DOWN = new HashSet<>(Arrays.asList(
        NORTH_D,
        SOUTH_D,
        EAST_D,
        WEST_D
    ));
    private static final Set<LegacyLevel> SIDE_DOWN_2 = new HashSet<>(Arrays.asList(
        NORTH_D_2,
        SOUTH_D_2,
        EAST_D_2,
        WEST_D_2
    ));

    public boolean isSide() {
        return !NON_SIDE.contains(this);
    }
    
    public boolean isSideAt() {
        return SIDE_AT.contains(this);
    }
    
    public boolean isSideUp() {
        return SIDE_UP.contains(this);
    }
    
    public boolean isSideDown() {
        return SIDE_DOWN.contains(this);
    }
    
    public boolean isSideDown2() {
        return SIDE_DOWN_2.contains(this);
    }
    
    public LegacyLevel sideUp() {
        return switch (this) {
            case NORTH -> NORTH_U;
            case SOUTH -> SOUTH_U;
            case EAST -> EAST_U;
            case WEST -> WEST_U;
            case NORTH_D -> NORTH;
            case SOUTH_D -> SOUTH;
            case EAST_D -> EAST;
            case WEST_D -> WEST;
            case NORTH_D_2 -> NORTH_D;
            case SOUTH_D_2 -> SOUTH_D;
            case EAST_D_2 -> EAST_D;
            case WEST_D_2 -> WEST_D;
            default -> null;
        };
    }
    
    public LegacyLevel sideDown() {
        return switch (this) {
            case NORTH_U -> NORTH;
            case SOUTH_U -> SOUTH;
            case EAST_U -> EAST;
            case WEST_U -> WEST;
            case NORTH -> NORTH_D;
            case SOUTH -> SOUTH_D;
            case EAST -> EAST_D;
            case WEST -> WEST_D;
            case NORTH_D -> NORTH_D_2;
            case SOUTH_D -> SOUTH_D_2;
            case EAST_D -> EAST_D_2;
            case WEST_D -> WEST_D_2;
            default -> null;
        };
    }
    
	public static LegacyLevel getOffset(Location start, Location end) {
		int diffX = end.getBlockX() - start.getBlockX();
		int diffY = end.getBlockY() - start.getBlockY();
		int diffZ = end.getBlockZ() - start.getBlockZ();
		for (LegacyLevel level : LegacyLevel.values()) {
			if (level.offsetX == diffX && level.offsetY == diffY && level.offsetZ == diffZ) {
				return level;
			}
		}
		return null;
	}
	
	public Location offset(Location loc) {
		return loc.add(offsetX, offsetY, offsetZ);
	}
    
    public static class LevelWrapper {
    	private LegacyLevel level;
    	
    	public LevelWrapper(LegacyLevel level) { 
    		this.level = level;
    	}
    	
    	public LegacyLevel getLevel() {
    		return level;
    	}
    	
    	public void setLevel(LegacyLevel level) {
    		this.level = level;
    	}
    }
}
