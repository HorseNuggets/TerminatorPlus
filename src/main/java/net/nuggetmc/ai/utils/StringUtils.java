package net.nuggetmc.ai.utils;

public class StringUtils {
    public static String trim16(String str) {
        return str.length() > 16 ? str.substring(0, 16) : str;
    }
}
