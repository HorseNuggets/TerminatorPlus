package net.nuggetmc.ai.utils;

import org.bukkit.GameMode;

public class PlayerUtils {

    public static boolean isVulnerableGamemode(GameMode mode) {
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }
}
