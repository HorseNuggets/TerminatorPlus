package net.nuggetmc.tplus.ui;

import net.nuggetmc.tplus.bot.Bot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class UIManager {
    // hashmaps for inventories and GUIs, inventories are to check against inventory interaction events
    // while GUIs are for tracking certain variables, like what page the player is on
    public static HashMap<Player, Inventory> playerInventories = new HashMap<Player, Inventory>();
    public static HashMap<Player, BotListGUI> playerBotListGUIs = new HashMap<Player, BotListGUI>();
    public static HashMap<Player, BotGUI> playerBotGUIs = new HashMap<Player, BotGUI>();




    public static void openBotListGUI(Player p, int page){

        BotListGUI botListGUI = new BotListGUI(p, page);
        UIManager.playerBotListGUIs.put(p, botListGUI);

        p.openInventory(botListGUI.fetch());
    }

    public static void openBotGUI(Player p, Bot bot){

        BotGUI botGUI = new BotGUI(p, bot);
        UIManager.playerBotGUIs.put(p, botGUI);
        p.openInventory(botGUI.fetch());
    }

}
