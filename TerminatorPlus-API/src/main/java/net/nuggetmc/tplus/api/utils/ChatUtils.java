package net.nuggetmc.tplus.api.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.NumberFormat;
import java.util.Locale;

public class ChatUtils {
    public static final String LINE = MiniMessage.miniMessage().deserialize("<gray>------------------------------------------------").toString();
    public static final String BULLET = "\u25AA";
    public static final String BULLET_FORMATTED = MiniMessage.miniMessage().deserialize("<gray>" + BULLET + "<white>").toString();
    public static final String EXCEPTION_MESSAGE = MiniMessage.miniMessage().deserialize("<red>An exception has occured. Please try again.").toString();

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public static final String ON = MiniMessage.miniMessage().deserialize("<green>").toString();
    public static final String OFF = MiniMessage.miniMessage().deserialize("<gray>").toString();

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
