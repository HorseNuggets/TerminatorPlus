package net.nuggetmc.tplus.ui.menu.buttons.impl;

import net.nuggetmc.tplus.ui.menu.buttons.Button;
import net.nuggetmc.tplus.ui.menu.menu.PaginatedMenu;
import net.nuggetmc.tplus.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PreviousPageButton extends Button {
    private final PaginatedMenu paginatedMenu;

    public PreviousPageButton(PaginatedMenu paginatedMenu) {
        this.paginatedMenu = paginatedMenu;
    }

    @Override
    public ItemStack getItem(Player player) {
        ItemBuilder item = new ItemBuilder(Material.ARROW);
        item.setName("&aPrevious page");

        if (this.paginatedMenu.getPage() == 1) {
            item.lore(ChatColor.RED + "This is the first page!");
        }else
            item.lore(ChatColor.GREEN + "Click to go to the previous page!");
        item.name(ChatColor.GREEN + "Previous Page");
        return item.build();
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
        if (this.paginatedMenu.getPage() == 1) {
            player.sendMessage(ChatColor.RED + "You're already on the first page!");
            return;
        }
        this.paginatedMenu.changePage(player, -1);
    }

    @Override
    public int getSlot() {
        return 36;
    }
}
