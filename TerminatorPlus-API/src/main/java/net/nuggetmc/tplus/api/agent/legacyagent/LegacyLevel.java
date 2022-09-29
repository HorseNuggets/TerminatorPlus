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

    public boolean isSide() {
        return !NON_SIDE.contains(this);
    }
}
