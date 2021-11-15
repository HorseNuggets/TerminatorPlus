package net.nuggetmc.tplus.ui.menu.buttons.impl;

import net.nuggetmc.tplus.ui.menu.buttons.Button;

import net.nuggetmc.tplus.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class CloseButton extends Button {
    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder(Material.BARRIER).name(ChatColor.RED + "Close").build();
    }

    @Override
    public int getSlot() {
        return 40;
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
        if (event.isShiftClick())
            return; //shift clicking the close button gives the player the barrier ?!
        player.getOpenInventory().close();
    }
}
