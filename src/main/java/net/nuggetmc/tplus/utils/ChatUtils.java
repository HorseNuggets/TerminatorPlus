package net.nuggetmc.tplus.utils;

import net.md_5.bungee.api.ChatColor;

import java.text.NumberFormat;
import java.util.Locale;

public class ChatUtils {
    public static final String LINE = translate("&7------------------------------------------------");
    public static final String BULLET_FORMATTED = translate("&7 ▪ &r");
    public static final String EXCEPTION_MESSAGE = translate("&cAn exception has occurred. Please try again.");
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    public static final String ON = ChatColor.GREEN.toString();
    public static final String OFF = ChatColor.GRAY.toString();

    public static String trim16(String str) {
        return str.length() > 16 ? str.substring(0, 16) : str;
    }

    public static String camelToDashed(String input) {
        StringBuilder result = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if (!Character.isUpperCase(ch))
                result.append(ch);

            if(Character.isUpperCase(ch))
                result.append("-").append(Character.toLowerCase(ch));
        }

        return result.toString();
    }

    public static String translate(String messageToTranslate) {
        return ChatColor.translateAlternateColorCodes('&', messageToTranslate);
    }
}
