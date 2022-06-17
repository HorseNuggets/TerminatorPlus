package net.nuggetmc.tplus.bot.agent.legacyagent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum LegacyLevel {
    ABOVE,
    BELOW,
    AT,
    AT_D,
    NORTH,
    SOUTH,
    EAST,
    WEST,
    NORTH_D,
    SOUTH_D,
    EAST_D,
    WEST_D;

    private static final Set<LegacyLevel> SIDE = new HashSet<>(Arrays.asList(
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NORTH_D,
        SOUTH_D,
        EAST_D,
        WEST_D
    ));

    public boolean isSide() {
        return SIDE.contains(this);
    }
}
