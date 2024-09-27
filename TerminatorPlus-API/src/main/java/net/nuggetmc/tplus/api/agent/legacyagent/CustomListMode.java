package net.nuggetmc.tplus.api.agent.legacyagent;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum CustomListMode {
    HOSTILE,
    RAIDER,
    MOB,
    CUSTOM;

    public static boolean isValid(String name) {
        return from(name) != null;
    }

    public static CustomListMode from(String name) {
        for (CustomListMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }

    public static String listModes() {
        return Arrays.stream(values()).map(e -> e.name().toLowerCase()).collect(Collectors.joining("|"));
    }
}
