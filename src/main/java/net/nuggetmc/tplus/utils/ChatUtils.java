package net.nuggetmc.tplus.utils;

import net.md_5.bungee.api.ChatColor;

import java.text.NumberFormat;
import java.util.Locale;

public class ChatUtils {
    public static final String LINE = ChatColor.GRAY + "------------------------------------------------";
    public static final String BULLET = "▪";
    public static final String BULLET_FORMATTED = ChatColor.GRAY + " ▪ " + ChatColor.RESET;
    public static final String EXCEPTION_MESSAGE = ChatColor.RED + "An exception has occured. Please try again.";

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public static final String ON = ChatColor.GREEN.toString();
    public static final String OFF = ChatColor.GRAY.toString();

    public static String trim16(String str) {
        return str.length() > 16 ? str.substring(0, 16) : str;
    }
}
