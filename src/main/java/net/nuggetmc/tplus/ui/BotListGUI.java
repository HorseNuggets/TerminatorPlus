package net.nuggetmc.tplus.ui;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.ui.menu.buttons.Button;
import net.nuggetmc.tplus.ui.menu.menu.PaginatedMenu;
import net.nuggetmc.tplus.utils.ItemBuilder;
import net.nuggetmc.tplus.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BotListGUI extends PaginatedMenu {
    @Override
    public String getPagesTitle(Player player) {
        return ChatColor.DARK_GREEN + "TerminatorPlus" + ChatColor.DARK_GRAY + " | Bots"; //for some reason getPage() breaks the gui if its over 1 ?!
    }

    @Override
    public List<Button> getPaginatedButtons(Player player) {
        Set<Bot> bots = TerminatorPlus.getInstance().getManager().fetch();
        List<Button> buttons = new ArrayList<>();
        int index = 0;
        for (Bot bot : bots) {
            buttons.add(new BotButton(++index,bots.size(), bot));
        }
        return buttons;
    }

    @Override
    public List<Button> getPersistantSlots(Player player) {
        return null;
    }
    private class BotButton extends Button {
        private int index,total;
        private Bot bot;

        private BotButton(int index,int total,Bot bot1) {
            this.index = index;
            this.total = total;
            this.bot = bot1;
        }

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(PlayerUtils.getPlayerHead(bot.getSkinName())).name(ChatColor.GOLD + bot.getName() + ChatColor.RESET + " - Bot " + index + " out of " + total)
                    .lore(bot.botLore()).build();
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            new BotManagerGUI(bot).open(player);
        }

        @Override
        public int getSlot() {
            return 0;
        }
    }
}
