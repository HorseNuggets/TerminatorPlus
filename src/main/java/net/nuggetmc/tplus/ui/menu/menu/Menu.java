package net.nuggetmc.tplus.ui.menu.menu;

import com.google.common.collect.Lists;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.ui.menu.MenuManager;
import net.nuggetmc.tplus.ui.menu.buttons.Button;
import net.nuggetmc.tplus.ui.menu.buttons.impl.CloseButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public abstract class Menu {
    public Plugin plugin = TerminatorPlus.getInstance();

    private List<Button> buttons = new ArrayList<>();

    public List<Button> getButtons() {
        return buttons;
    }


    private boolean updateInTask = false;

    public boolean isUpdateInTask() {
        return updateInTask;
    }

    public void setUpdateInTask(boolean updateInTask) {
        this.updateInTask = updateInTask;
    }

    public abstract List<Button> getButtons(Player player);

    public abstract String getName(Player player);

    public List<Button> getFinalButtons(Player player){
        List<Button> list = getButtons(player);
        if (list == null)
            list = new ArrayList<>();
        Button backButton = getBackButton(player);
        if (backButton != null)
            list.add(backButton);
        return list;
    }

    private boolean cancel = true;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public Menu previous;

    public Menu getPrevious() {
        return previous;
    }

    public void setPrevious(Menu previous) {
        this.previous = previous;
    }

    public void open(Player player) {
        Menu previous = MenuManager.getOpenedMenus().get(player.getUniqueId());
        if (previous != null) {
            setPrevious(previous);
            previous.onClose(player);
            MenuManager.getOpenedMenus().remove(player.getUniqueId());
        }

        this.buttons = this.getFinalButtons(player);
        String title = this.getName(player);

        if (title.length() > 35) title = title.substring(0, 35);
        title = ChatColor.translateAlternateColorCodes('&',title);

        if (player.getOpenInventory() != null) {
            player.closeInventory();
        }

        Inventory inventory = Bukkit.createInventory(player, this.getInventorySize(this.buttons), title);

        this.buttons.forEach(button -> {
            inventory.setItem(button.getSlot(), button.getItem(player));
            if (button.getSlots() != null) {
                Arrays.stream(button.getSlots()).forEach(extra -> {
                    if(shouldKeepExtra(extra)) inventory.setItem(extra, button.getItem(player));
                });
            }
        });

        MenuManager.getOpenedMenus().put(player.getUniqueId(), this);
        player.openInventory(inventory);

        this.onOpen(player);
    }
    private boolean shouldKeepExtra(int slot){
        for (Button button1 : this.buttons) {
            if (button1.getSlot() == slot)
                return false;
            else if (Lists.newArrayList(button1.getSlots()).contains(slot))
                return true;
        }
        return true;
    }
    public Button getCloseButton(){
        return new CloseButton();
    }
    public void update(Player player) {
        this.buttons = this.getFinalButtons(player);
        String title = this.getName(player);

        if (title.length() > 32) title = title.substring(0, 32);
        title = ChatColor.translateAlternateColorCodes('&',title);

        boolean passed = false;
        Inventory inventory = null;
        Menu currentlyOpenedMenu = MenuManager.getOpenedMenus().get(player.getUniqueId());
        Inventory current = player.getOpenInventory().getTopInventory();

        if (currentlyOpenedMenu != null && ChatColor.translateAlternateColorCodes('&',currentlyOpenedMenu.getName(player))
                .equals(player.getOpenInventory().getTitle()) && current.getSize() == this.getInventorySize(this.buttons)) {
            inventory = current;
            passed = true;
        }

        if (inventory == null) {
            inventory = Bukkit.createInventory(player, this.getInventorySize(this.buttons), title);
        }

        /**
         * TemporaryInventory
         * Used to prevent item flickering because 'inventory' is live player inventory
         */
        Inventory temporaryInventory = Bukkit.createInventory(player, inventory.getSize(), player.getOpenInventory().getTitle());

        this.buttons.forEach(slot -> {
            temporaryInventory.setItem(slot.getSlot(), slot.getItem(player));

            if (slot.getSlots() != null) {
                Arrays.stream(slot.getSlots()).forEach(extra -> {
                    temporaryInventory.setItem(extra, slot.getItem(player));
                });
            }
        });

        MenuManager.getOpenedMenus().remove(player.getUniqueId());
        MenuManager.getOpenedMenus().put(player.getUniqueId(), this);

        inventory.setContents(temporaryInventory.getContents());
        if (passed) {
            player.updateInventory();
        } else {
            player.openInventory(inventory);
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',"&cOpened new inventory"));
        }

        this.onOpen(player);
    }

    public int getInventorySize(List<Button> buttons) {
        int highest = 0;
        if (!buttons.isEmpty()) {
            highest = buttons.stream().sorted(Comparator.comparingInt(Button::getSlot).reversed()).map(Button::getSlot).collect(Collectors.toList()).get(0);
        }
        for (Button button : buttons) {
            if (button.getSlots() != null) {
                for (int i = 0; i < button.getSlots().length; i++) {
                    if (button.getSlots()[i] > highest) {
                        highest = button.getSlots()[i];
                    }
                }
            }
        }
        return (int) (Math.ceil((highest + 1) / 9D) * 9D);
    }

    public boolean hasSlot(int value) {
        return this.buttons.stream()
                .filter(slot -> slot.getSlot() == value || slot.getSlots() != null
                        && Arrays.stream(slot.getSlots()).anyMatch(i -> i == value && shouldKeepExtra(i)))
                .findFirst().orElse(null) != null;
    }

    public Button getSlot(int value) {
        return this.buttons.stream()
                .filter(slot -> slot.getSlot() == value || slot.getSlots() != null
                        && Arrays.stream(slot.getSlots()).anyMatch(i -> i == value && shouldKeepExtra(i)))
                .findFirst().orElse(null);
    }
    public Button getBackButton(Player player){
        return null;
    }

    public void onOpen(Player player) {

    }

    public void onClose(Player player) {
        MenuManager.getLastOpenedMenus().remove(player.getUniqueId());
        MenuManager.getLastOpenedMenus().put(player.getUniqueId(), this);
    }

    public List<Button> getToolbarButtons(){
        return null;
    }
    public List<Button> getFinalExtraButtons(Player p){
        List<Button> buttons = new ArrayList<>();
        if (getToolbarButtons() != null){
            buttons.addAll(getToolbarButtons());
        }
        if (getBackButton(p) != null)
            buttons.add(getBackButton(p));
        return buttons;
    }
    public boolean doesButtonExist(List<Button> buttons,int i){ //
        return buttons.stream().filter(button ->{
            if (button.getSlot() == i){
                return true;
            }
            for (int slot : button.getSlots()) {
                if (slot == i)
                    return true;
            }
            return false;
        }).findFirst().orElse(null) != null;
    }
    public int[] genPlaceholderSpots(IntStream intStream, int... skipInput){
        List<Integer> list = new ArrayList<>(),l1 = new ArrayList<>();
        if (skipInput != null){
            for (int i : skipInput) {
                l1.add(i);
            }
        }
        intStream.forEach(i ->{
            if (!l1.contains(i)){
                list.add(i);
            }
        });
        return list.stream().mapToInt(i -> i).toArray();
    }
}
