package net.nuggetmc.ai.utils;

import org.bukkit.GameMode;

public class PlayerUtils {

    public static boolean allTargetable;

    public static boolean isTargetable(GameMode mode) {
        return allTargetable || mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    public static void setAllTargetable(boolean b) {
        allTargetable = b;
    }

    public static boolean getAllTargetable() {
        return allTargetable;
    }
}
