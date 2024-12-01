package net.nuggetmc.tplus.api.utils;

import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static double getLegacyAttackDamage(ItemStack item) {
        return switch (item.getType()) {
            case WOODEN_SHOVEL, GOLDEN_SHOVEL, WOODEN_HOE, GOLDEN_HOE, STONE_HOE, IRON_HOE, DIAMOND_HOE,
                 NETHERITE_HOE -> 1;
            case WOODEN_PICKAXE, GOLDEN_PICKAXE, STONE_SHOVEL -> 2;
            case WOODEN_AXE, GOLDEN_AXE, STONE_PICKAXE, IRON_SHOVEL -> 3;
            case WOODEN_SWORD, GOLDEN_SWORD, STONE_AXE, IRON_PICKAXE, DIAMOND_SHOVEL -> 4;
            case STONE_SWORD, IRON_AXE, DIAMOND_PICKAXE, NETHERITE_SHOVEL -> 5;
            case IRON_SWORD, DIAMOND_AXE, NETHERITE_PICKAXE -> 6;
            case DIAMOND_SWORD, NETHERITE_AXE -> 7;
            case NETHERITE_SWORD -> 8;
            default -> 0.25;
        };
    }
}
