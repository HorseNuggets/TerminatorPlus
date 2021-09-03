package net.nuggetmc.tplus.ui.event;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.ui.BotListGUI;
import net.nuggetmc.tplus.ui.UIManager;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.SkullMeta;

import static net.nuggetmc.tplus.TerminatorPlus.getInstance;

public class InteractEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player p = (Player) event.getWhoClicked();
        if (event.getClickedInventory().equals(UIManager.playerInventories.get(p))){
            event.setCancelled(true);

            if (event.getSlot() == 48 && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Previous Page")){ // Previous Page
                UIManager.openBotListGUI(p, UIManager.playerBotListGUIs.get(p).getPage() - 1);
                return;
            }
            if (event.getSlot() == 48 && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Go Back")){ // Previous Page
                UIManager.openBotListGUI(p, UIManager.playerBotListGUIs.get(p).getPage());
                return;
            }
            if (event.getSlot() == 49){ // Exit Menu
                p.closeInventory();
                return;
            }
            if (event.getSlot() == 50 && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Next Page")){ // Next Page
                UIManager.openBotListGUI(p, UIManager.playerBotListGUIs.get(p).getPage() + 1);
                return;
            }
            if ((event.getSlot() >= 0 || event.getSlot() < 45) && event.getCurrentItem().getItemMeta() instanceof SkullMeta){
                UIManager.openBotGUI(p, UIManager.playerBotListGUIs.get(p).getBot(event.getSlot() + (UIManager.playerBotListGUIs.get(p).getPage()-1)*45));
                return;
            }


            if (event.getSlot() == 31 && event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED + "Remove Bot")){
                Bot bot = UIManager.playerBotGUIs.get(p).getBot();
                bot.reset();
                bot.removeVisually();
                getInstance().getManager().remove(bot);
                p.sendMessage("Bot has been removed!");
                UIManager.openBotListGUI(p, UIManager.playerBotListGUIs.get(p).getPage());
                return;
            }
        }


    }
}
