package net.nuggetmc.tplus.ui;

import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.ui.menu.BotListGUI;
import net.nuggetmc.tplus.ui.menu.BotManagerGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class UIManager {
    public static void openBotListGUI(Player p){
        new BotListGUI().open(p);
    }

    public static void openBotGUI(Player p, Bot bot){
        new BotManagerGUI(bot).open(p);
    }

}
