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

public class NextPageButton extends Button {
    private final PaginatedMenu paginatedMenu;

    public NextPageButton(PaginatedMenu paginatedMenu) {
        this.paginatedMenu = paginatedMenu;
    }

    @Override
    public ItemStack getItem(Player player) {
        ItemBuilder item = new ItemBuilder(Material.ARROW);
        if (this.paginatedMenu.getPage() < this.paginatedMenu.getPages(player)) { //next page
            item.lore(
                    ChatColor.GREEN + "Click to go to the next page"
            );
        }else item.lore(ChatColor.RED + "This is the last page!");
        item.name(ChatColor.GREEN + "Next Page");
        return item.build();
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
        if (!(this.paginatedMenu.getPage() < this.paginatedMenu.getPages(player))) {
            player.sendMessage(ChatColor.RED + "You're already on the last page!");
            return;
        }
        this.paginatedMenu.changePage(player, 1);
    }

    @Override
    public int getSlot() {
        return 44;
    }
}
