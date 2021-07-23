package net.nuggetmc.ai.utils;

import net.md_5.bungee.api.ChatColor;

public class StringUtilities {

    public static final String ON = ChatColor.GREEN.toString();
    public static final String OFF = ChatColor.GRAY.toString();

    public static String trim16(String str) {
        return str.length() > 16 ? str.substring(0, 16) : str;
    }
}
