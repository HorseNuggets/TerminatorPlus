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
    private final DecimalFormat formatter;
    private Inventory botGUI;
    private Bot bot;

    public Inventory createBotGUI(Player p, Bot bot) {
        this.bot = bot;
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "TerminatorPlus" + ChatColor.DARK_GRAY + " | Bots | " + ChatColor.AQUA + bot.getName());
        ItemStack backgroundItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta backgroundItemMeta = backgroundItem.getItemMeta();
        backgroundItemMeta.setDisplayName(" ");
        backgroundItem.setItemMeta(backgroundItemMeta);

        // set entire inventory to black glass planes to create a pseudo-background
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, backgroundItem);
        }

        ItemStack head = PlayerUtils.getPlayerHead(bot.getName());
        ItemMeta headMeta = head.getItemMeta();
        headMeta.setDisplayName(ChatColor.GOLD + bot.getName());



        // Lore to add to head, stats about the bot.

        String world = bot.getBukkitEntity().getWorld().getName();
        Location loc = bot.getLocation();

        String location = org.bukkit.ChatColor.AQUA + formatter.format(loc.getBlockX()) + ", " + formatter.format(loc.getBlockY()) + ", " + formatter.format(loc.getBlockZ());

        Vector vel = bot.getVelocity();
        String velocity = org.bukkit.ChatColor.AQUA + formatter.format(vel.getX()) + ", " + formatter.format(vel.getY()) + ", " + formatter.format(vel.getZ());


        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "World - " + ChatColor.YELLOW + world);
        lore.add(ChatColor.WHITE + "Location - " + ChatColor.AQUA + location);
        lore.add(ChatColor.WHITE + "Velocity - " + ChatColor.AQUA + velocity);
        lore.add(ChatColor.WHITE + "Health - " + ChatColor.RED + bot.getHealth());
        lore.add(ChatColor.WHITE + "Kills - " + ChatColor.RED + bot.getKills());

        headMeta.setLore(lore);



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
        this.formatter = new DecimalFormat("0.##");
        botGUI = createBotGUI(p, bot);
    }

    // public accessor method for getting the inventory (GUI)
    public Inventory fetch(){
        return botGUI;
    }

    // public accessor method for bot variable
    public Bot getBot(){ return bot; }
}
