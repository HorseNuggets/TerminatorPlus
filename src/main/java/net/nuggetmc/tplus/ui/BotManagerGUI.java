package net.nuggetmc.tplus.ui;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.ui.UIManager;
import net.nuggetmc.tplus.ui.menu.buttons.Button;
import net.nuggetmc.tplus.ui.menu.buttons.PlaceholderButton;
import net.nuggetmc.tplus.ui.menu.buttons.impl.BackButton;
import net.nuggetmc.tplus.ui.menu.buttons.impl.CloseButton;
import net.nuggetmc.tplus.ui.menu.menu.Menu;
import net.nuggetmc.tplus.utils.ItemBuilder;
import net.nuggetmc.tplus.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static net.nuggetmc.tplus.TerminatorPlus.getInstance;

public class BotManagerGUI extends Menu {
    public BotManagerGUI (Bot bot){
        this.bot = bot;
    }
    private Bot bot;
    @Override
    public List<Button> getButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new Placeholder());
        buttons.add(new BotButton());
        buttons.add(new RemoveBotButton());
        buttons.add(new CloseButton(){
            @Override
            public int getSlot() {
                return 49;
            }
        });
        return buttons;
    }


    @Override
    public String getName(Player player) {
        return ChatColor.DARK_GREEN + "TerminatorPlus" + ChatColor.DARK_GRAY + " | Bots | " + ChatColor.AQUA + bot.getName();
    }

    @Override
    public Button getBackButton(Player player) {
        return new BackButton() {
            @Override
            public void clicked(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
                previous.open(player);
            }

            @Override
            public int getSlot() {
                return 48;
            }
        };
    }

    @Override
    public Button getCloseButton() {
        return null; //wtf
    }

    private class Placeholder extends PlaceholderButton {
        @Override
        public int[] getSlots() {
            return genPlaceholderSpots(IntStream.range(0,54),13,31,48,49);
        }
    }
    private class BotButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(PlayerUtils.getPlayerHead(bot.getSkinName())).name(ChatColor.GOLD + bot.getName())
                    .lore(bot.botLore()).build();
        }

        @Override
        public int getSlot() {
            return 13;
        }
    }
    private class RemoveBotButton extends Button {

        @Override
        public ItemStack getItem(Player player) {
            return new ItemBuilder(Material.TNT).name(ChatColor.RED + "Remove Bot").build();
        }

        @Override
        public int getSlot() {
            return 31;
        }

        @Override
        public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
            bot.reset();
            bot.removeVisually();
            getInstance().getManager().remove(bot);
            player.sendMessage("Bot " + ChatColor.GREEN + bot.getName() + ChatColor.RESET + " has been removed!");
            previous.open(player);
        }
    }
}
