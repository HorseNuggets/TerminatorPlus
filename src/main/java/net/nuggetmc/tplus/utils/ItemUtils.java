package net.nuggetmc.tplus.utils;

import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static double getLegacyAttackDamage(ItemStack item) {
        switch (item.getType()) {
            default:
                return 0.25;

            case WOODEN_SHOVEL:
            case GOLDEN_SHOVEL:
            case WOODEN_HOE:
            case GOLDEN_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case DIAMOND_HOE:
            case NETHERITE_HOE:
                return 1;

            case WOODEN_PICKAXE:
            case GOLDEN_PICKAXE:
            case STONE_SHOVEL:
                return 2;

            case WOODEN_AXE:
            case GOLDEN_AXE:
            case STONE_PICKAXE:
            case IRON_SHOVEL:
                return 3;

            case WOODEN_SWORD:
            case GOLDEN_SWORD:
            case STONE_AXE:
            case IRON_PICKAXE:
            case DIAMOND_SHOVEL:
                return 4;

            case STONE_SWORD:
            case IRON_AXE:
            case DIAMOND_PICKAXE:
            case NETHERITE_SHOVEL:
                return 5;

            case IRON_SWORD:
            case DIAMOND_AXE:
            case NETHERITE_PICKAXE:
                return 6;

            case DIAMOND_SWORD:
            case NETHERITE_AXE:
                return 7;

            case NETHERITE_SWORD:
                return 8;
        }
    }
}
