package net.nuggetmc.tplus.ui;

import net.nuggetmc.tplus.bot.Bot;
import org.bukkit.entity.Player;

public class UIManager {
    public static void openBotListGUI(Player p){
        new BotListGUI().open(p);
    }

    public static void openBotGUI(Player p, Bot bot){
        new BotManagerGUI(bot).open(p);
    }

}
