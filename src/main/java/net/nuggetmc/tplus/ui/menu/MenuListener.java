package net.nuggetmc.tplus.ui.menu;

import net.nuggetmc.tplus.ui.menu.buttons.Button;
import net.nuggetmc.tplus.ui.menu.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        Menu menu = MenuManager.getOpenedMenus().get(player.getUniqueId());

        if (menu == null) return;

        if (menu.isCancel())
            event.setCancelled(true);

        if (event.getSlot() != event.getRawSlot()) return;
        if (!menu.hasSlot(event.getSlot())) return;

        Button slot = menu.getSlot(event.getSlot());
        slot.onClick(player, event.getSlot(), event.getClick(),event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        Menu menu = MenuManager.getOpenedMenus().get(player.getUniqueId());

        if (menu == null) return;

        menu.onClose(player);
        MenuManager.getOpenedMenus().remove(player.getUniqueId());
    }
}
