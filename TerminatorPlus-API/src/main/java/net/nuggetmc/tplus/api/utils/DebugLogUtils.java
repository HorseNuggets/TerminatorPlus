package net.nuggetmc.tplus.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.ServerOperator;

import java.util.Arrays;

public class DebugLogUtils {
    public static final String PREFIX = ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET;

    public static void log(Object... objects) {
        String[] values = fromStringArray(objects);
        String message = PREFIX + String.join(" ", values);

        Bukkit.getConsoleSender().sendMessage(message);
        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(p -> p.sendMessage(message));
    }

    public static String[] fromStringArray(Object[] objects) {
        return Arrays.stream(objects).map(String::valueOf).toArray(String[]::new);
    }
}
