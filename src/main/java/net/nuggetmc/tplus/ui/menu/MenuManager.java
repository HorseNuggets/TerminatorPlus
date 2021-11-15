package net.nuggetmc.tplus.ui.menu;

import net.nuggetmc.tplus.ui.menu.menu.Menu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {
    
    private static Map<UUID, Menu> openedMenus = new HashMap<>();
    
    private static Map<UUID, Menu> lastOpenedMenus = new HashMap<>();

    public static Map<UUID, Menu> getLastOpenedMenus() {
        return lastOpenedMenus;
    }

    public static Map<UUID, Menu> getOpenedMenus() {
        return openedMenus;
    }
}
