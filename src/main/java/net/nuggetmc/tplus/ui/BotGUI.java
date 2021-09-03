package net.nuggetmc.tplus.ui;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static net.nuggetmc.tplus.TerminatorPlus.getInstance;

public class BotGUI {
    private Inventory botGUI;
    private Bot bot;

    public Inventory createBotGUI(Player p, Bot bot) {
        this.bot = bot;
        String invTitle = ChatColor.DARK_GREEN + "TerminatorPlus" + ChatColor.DARK_GRAY + " | Bots | " + ChatColor.AQUA + bot.getName();
        if (invTitle.length() > 35){
            invTitle = invTitle.substring(0, 35) + "...";
        }

        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, invTitle);

        // Create item that acts as the filler for the UI, which in this case is a black glass plane.
        ItemStack backgroundItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta backgroundItemMeta = backgroundItem.getItemMeta();
        backgroundItemMeta.setDisplayName(" ");
        backgroundItem.setItemMeta(backgroundItemMeta);

        // Set entire inventory to black glass planes to create a pseudo-background
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, backgroundItem);
        }

        // Creating the bot's head with the correct skin, not just the skin linked to the name the bot uses.
        ItemStack head = PlayerUtils.getPlayerHead(bot.getSkinName());
        ItemMeta headMeta = head.getItemMeta();
        headMeta.setDisplayName(ChatColor.GOLD + bot.getName());



        // Fetching info about the bot
        // and adding bot info to the lore of the head.
        headMeta.setLore(bot.botLore());



        head.setItemMeta(headMeta);
        inventory.setItem(13, head);


        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);


        // remove bot button
        item.setType(Material.TNT);
        meta.setDisplayName(ChatColor.RED + "Remove Bot");
        item.setItemMeta(meta);
        inventory.setItem(31, item);


        // back button
        item.setType(Material.ARROW);
        meta.setDisplayName(ChatColor.GOLD + "Go Back");
        item.setItemMeta(meta);
        inventory.setItem(48, item);

        // close menu button
        item.setType(Material.BARRIER);
        meta.setDisplayName(ChatColor.RED + "Close Menu");
        item.setItemMeta(meta);
        inventory.setItem(49, item);

        // remove any maps for previous player inventories
        try{ UIManager.playerInventories.remove(p); }
        catch(Exception e){ }

        // add new map for player inventory
        UIManager.playerInventories.put(p, inventory);

        // remove any maps for previous player GUIs
        try{ UIManager.playerBotGUIs.remove(p); }
        catch(Exception e){ }

        return inventory;
    }

    public BotGUI(Player p, Bot bot){
        botGUI = createBotGUI(p, bot);
    }



    // public accessor method for getting the inventory (GUI)
    public Inventory fetch(){
        return botGUI;
    }

    // public accessor method for bot variable
    public Bot getBot(){ return bot; }
}
