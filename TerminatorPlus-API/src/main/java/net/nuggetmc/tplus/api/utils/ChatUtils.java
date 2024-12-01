package net.nuggetmc.tplus.api.utils;

import net.kyori.adventure.text.format.NamedTextColor;

import java.text.NumberFormat;
import java.util.Locale;

public class ChatUtils {
    public static final String LINE = NamedTextColor.GRAY + "------------------------------------------------";
    public static final String BULLET = "\u25AA";
    public static final String BULLET_FORMATTED = NamedTextColor.GRAY + BULLET + NamedTextColor.WHITE;
    public static final String EXCEPTION_MESSAGE = NamedTextColor.RED + "An exception has occured. Please try again.";

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public static final String ON = NamedTextColor.GREEN.toString();
    public static final String OFF = NamedTextColor.GRAY.toString();

    public static String trim16(String str) {
        return str.length() > 16 ? str.substring(0, 16) : str;
    }

    public static String camelToDashed(String input) {
        StringBuilder result = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                result.append("-").append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
