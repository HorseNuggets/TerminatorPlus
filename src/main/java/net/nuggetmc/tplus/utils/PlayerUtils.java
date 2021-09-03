package net.nuggetmc.tplus.utils;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.TerminatorPlus;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerUtils {

    public static boolean isInvincible(GameMode mode) {
        return mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE && mode != null;
    }

    private static final Set<String> USERNAME_CACHE = new HashSet<>();

    public static String randomName() {
        if (USERNAME_CACHE.isEmpty()) {
            fillUsernameCache();
        }

        return MathUtils.getRandomSetElement(USERNAME_CACHE);
    }

    public static void fillUsernameCache() {
        String file = TerminatorPlus.getInstance().getServer().getWorldContainer().getAbsolutePath();
        file = file.substring(0, file.length() - 1) + "usercache.json";

        JSONParser parser = new JSONParser();

        try {
            JSONArray array = (JSONArray) parser.parse(new FileReader(file));

            for (Object obj : array) {
                JSONObject jsonOBJ = (JSONObject) obj;
                String username = (String) jsonOBJ.get("name");

                USERNAME_CACHE.add(username);
            }
        }

        catch (IOException | ParseException e) {
            Debugger.log("Failed to fetch from the usercache.");
        }
    }

    public static Location findAbove(Location loc, int amount) {
        boolean check = false;

        for (int i = 0; i <= amount; i++) {
            if (loc.clone().add(0, i, 0).getBlock().getType().isSolid()) {
                check = true;
                break;
            }
        }

        if (check) {
            return loc;
        } else {
            return loc.clone().add(0, amount, 0);
        }
    }

    public static Location findBottom(Location loc) {
        loc.setY(loc.getBlockY());

        for (int i = 0; i < 255; i++) {
            Location check = loc.clone().add(0, -i, 0);

            if (check.getY() <= 0) {
                break;
            }

            if (check.getBlock().getType().isSolid()) {
                return check.add(0, 1, 0);
            }
        }

        return loc;
    }

    // method to return the head of a player, given their name
    @SuppressWarnings("deprecation")
    public static ItemStack getPlayerHead(String player) {
        boolean isNewVersion = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList()).contains("PLAYER_HEAD");

        Material type = Material.matchMaterial(isNewVersion? "PLAYER_HEAD" : "SKULL_ITEM");
        ItemStack item = new ItemStack(type, 1);

        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwner(player);
        meta.setDisplayName(ChatColor.DARK_PURPLE + player + "'s " + ChatColor.WHITE + "head");

        item.setItemMeta(meta);

        return item;
    }
}
