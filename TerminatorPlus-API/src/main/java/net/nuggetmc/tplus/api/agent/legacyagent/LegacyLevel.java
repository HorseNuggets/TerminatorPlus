package net.nuggetmc.tplus.api.agent.legacyagent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum LegacyLevel {
    ABOVE,
    BELOW,
    AT,
    AT_D,
    NORTH_U,
    SOUTH_U,
    EAST_U,
    WEST_U,
    NORTH,
    SOUTH,
    EAST,
    WEST,
    NORTH_D,
    SOUTH_D,
    EAST_D,
    WEST_D,
    NORTH_D_2,
    SOUTH_D_2,
    EAST_D_2,
    WEST_D_2;

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
    	switch(this) {
    		case NORTH: return NORTH_U;
    		case SOUTH: return SOUTH_U;
    		case EAST: return EAST_U;
    		case WEST: return WEST_U;
    		case NORTH_D: return NORTH;
    		case SOUTH_D: return SOUTH;
    		case EAST_D: return EAST;
    		case WEST_D: return WEST;
    		case NORTH_D_2: return NORTH_D;
    		case SOUTH_D_2: return SOUTH_D;
    		case EAST_D_2: return EAST_D;
    		case WEST_D_2: return WEST_D;
    		default:
    			return null;
    	}
    }
    
    public LegacyLevel sideDown() {
    	switch(this) {
    		case NORTH_U: return NORTH;
    		case SOUTH_U: return SOUTH;
    		case EAST_U: return EAST;
    		case WEST_U: return WEST;
    		case NORTH: return NORTH_D;
    		case SOUTH: return SOUTH_D;
    		case EAST: return EAST_D;
    		case WEST: return WEST_D;
    		case NORTH_D: return NORTH_D_2;
    		case SOUTH_D: return SOUTH_D_2;
    		case EAST_D: return EAST_D_2;
    		case WEST_D: return WEST_D_2;
    		default:
    			return null;
    	}
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
