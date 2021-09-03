package net.nuggetmc.tplus.ui;

import net.nuggetmc.tplus.bot.Bot;

import net.md_5.bungee.api.ChatColor;

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
import java.util.HashMap;
import java.util.List;

import static net.nuggetmc.tplus.TerminatorPlus.getInstance;

public class BotListGUI {
    private final DecimalFormat formatter;
    private Bot[] bots = new Bot[getInstance().getManager().fetch().size()];

    private int page;



    private Inventory botListGUI;
    public Inventory createBotListGUI(Player p, int page){
        // prevent negative pages from occurring
        if (page < 1){
            page = 1;
        }
        // prevent infinite pages
        if (((page - 1) * 45) > getInstance().getManager().fetch().size()){
            page -= 1;
        }

        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "TerminatorPlus" + ChatColor.DARK_GRAY + " | Bots | " + ChatColor.AQUA + "Page " + page);
        this.page = page;
        ItemStack backgroundItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta backgroundItemMeta = backgroundItem.getItemMeta();
        backgroundItemMeta.setDisplayName(" ");
        backgroundItem.setItemMeta(backgroundItemMeta);

        // set entire inventory to black glass planes to create a pseudo-background
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, backgroundItem);
        }

        // change list of bots from set to array so it can be indexed easier
        int index = 0;
        for (Bot bot : getInstance().getManager().fetch()){
            bots[index] = bot;
            index++;
        }

        // start putting playerHeads in the inventory to represent bots
        for (int i = (page-1)*45; i < (page*45) && i < bots.length; i++){
            Bot bot = bots[i];
            ItemStack head = PlayerUtils.getPlayerHead(bot.getSkinName());
            ItemMeta headMeta = head.getItemMeta();
            headMeta.setDisplayName(ChatColor.GOLD + bot.getSkinName() + ChatColor.WHITE + " - Bot " + (i + 1) + " of " + bots.length);



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
            inventory.setItem(i - (page-1)*45, head);
        }

        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);

        if (page > 1){
            // back button
            item.setType(Material.ARROW);
            meta.setDisplayName(ChatColor.GOLD + "Previous Page");
            item.setItemMeta(meta);
            inventory.setItem(48, item);
        }

        if ((page*45) < bots.length){
            // forward button
            item.setType(Material.ARROW);
            meta.setDisplayName(ChatColor.GOLD + "Next Page");
            item.setItemMeta(meta);
            inventory.setItem(50, item);
        }

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
        try{ UIManager.playerBotListGUIs.remove(p); }
        catch(Exception e){ }

        return inventory;
    }

    public BotListGUI(Player p, int page){
        this.formatter = new DecimalFormat("0.##");
        botListGUI = createBotListGUI(p,page);
    }


    public Inventory fetch(){
        return botListGUI;
    }

    public int getPage(){
        return this.page;
    }

    public Bot getBot(int index){
        return bots[index];
    }



}
