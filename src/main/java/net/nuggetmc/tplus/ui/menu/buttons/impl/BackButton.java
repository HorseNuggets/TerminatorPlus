package net.nuggetmc.tplus.ui.menu.buttons.impl;

import net.nuggetmc.tplus.ui.menu.buttons.Button;

import net.nuggetmc.tplus.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class BackButton extends Button {
    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder(Material.ARROW).name(ChatColor.GREEN + "Back").build();
    }

    @Override
    public int getSlot() {
        return 39;
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType, InventoryClickEvent event) {
        clicked(player, slot, clickType,event);
    }
    public abstract void clicked(Player player,int slot,ClickType clickType,InventoryClickEvent event);
}
