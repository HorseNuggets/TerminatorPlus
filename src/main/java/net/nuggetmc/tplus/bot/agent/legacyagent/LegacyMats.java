package net.nuggetmc.tplus.bot.agent.legacyagent;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LegacyMats {

    public static final Set<Material> AIR = new HashSet<>(Arrays.asList(
        Material.WATER,
        Material.OAK_TRAPDOOR,
        Material.FIRE,
        Material.LAVA,
        Material.SNOW,
        Material.CAVE_AIR,
        Material.VINE,
        Material.FERN,
        Material.LARGE_FERN,
        Material.GRASS,
        Material.TALL_GRASS,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SUNFLOWER,
        Material.AIR,
        Material.VOID_AIR,
        Material.FIRE,
        Material.SOUL_FIRE
    ));

    public static final Set<Material> NO_CRACK = new HashSet<>(Arrays.asList(
        Material.WATER,
        Material.FIRE,
        Material.LAVA,
        Material.CAVE_AIR,
        Material.SOUL_FIRE
    ));

    public static final Set<Material> SHOVEL = new HashSet<>(Arrays.asList(
        Material.DIRT,
        Material.GRAVEL,
        Material.SAND,
        Material.SNOW
    ));

    public static final Set<Material> AXE = new HashSet<>(Arrays.asList(
        Material.OAK_PLANKS, Material.OAK_DOOR, Material.OAK_FENCE, Material.OAK_FENCE_GATE, Material.OAK_LOG, Material.OAK_PLANKS,
        Material.OAK_SIGN, Material.OAK_SLAB, Material.OAK_STAIRS, Material.OAK_TRAPDOOR, Material.OAK_WALL_SIGN, Material.OAK_WOOD,
        Material.DARK_OAK_PLANKS, Material.DARK_OAK_DOOR, Material.DARK_OAK_FENCE, Material.DARK_OAK_FENCE_GATE, Material.DARK_OAK_LOG, Material.DARK_OAK_PLANKS,
        Material.DARK_OAK_SIGN, Material.DARK_OAK_SLAB, Material.DARK_OAK_STAIRS, Material.DARK_OAK_TRAPDOOR, Material.DARK_OAK_WALL_SIGN, Material.DARK_OAK_WOOD,
        Material.ACACIA_PLANKS, Material.ACACIA_DOOR, Material.ACACIA_FENCE, Material.ACACIA_FENCE_GATE, Material.ACACIA_LOG, Material.ACACIA_PLANKS,
        Material.ACACIA_SIGN, Material.ACACIA_SLAB, Material.ACACIA_STAIRS, Material.ACACIA_TRAPDOOR, Material.ACACIA_WALL_SIGN, Material.ACACIA_WOOD,
        Material.BIRCH_PLANKS, Material.BIRCH_DOOR, Material.BIRCH_FENCE, Material.BIRCH_FENCE_GATE, Material.BIRCH_LOG, Material.BIRCH_PLANKS,
        Material.BIRCH_SIGN, Material.BIRCH_SLAB, Material.BIRCH_STAIRS, Material.BIRCH_TRAPDOOR, Material.BIRCH_WALL_SIGN, Material.BIRCH_WOOD,
        Material.JUNGLE_PLANKS, Material.JUNGLE_DOOR, Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE, Material.JUNGLE_LOG, Material.JUNGLE_PLANKS,
        Material.JUNGLE_SIGN, Material.JUNGLE_SLAB, Material.JUNGLE_STAIRS, Material.JUNGLE_TRAPDOOR, Material.JUNGLE_WALL_SIGN, Material.JUNGLE_WOOD,
        Material.SPRUCE_PLANKS, Material.SPRUCE_DOOR, Material.SPRUCE_FENCE, Material.SPRUCE_FENCE_GATE, Material.SPRUCE_LOG, Material.SPRUCE_PLANKS,
        Material.SPRUCE_SIGN, Material.SPRUCE_SLAB, Material.SPRUCE_STAIRS, Material.SPRUCE_TRAPDOOR, Material.SPRUCE_WALL_SIGN, Material.SPRUCE_WOOD,
        Material.CRIMSON_PLANKS, Material.CRIMSON_DOOR, Material.CRIMSON_FENCE, Material.CRIMSON_FENCE_GATE, Material.CRIMSON_PLANKS,
        Material.CRIMSON_SIGN, Material.CRIMSON_SLAB, Material.CRIMSON_STAIRS, Material.CRIMSON_TRAPDOOR, Material.CRIMSON_WALL_SIGN,
        Material.WARPED_PLANKS, Material.WARPED_DOOR, Material.WARPED_FENCE, Material.WARPED_FENCE_GATE, Material.WARPED_PLANKS,
        Material.WARPED_SIGN, Material.WARPED_SLAB, Material.WARPED_STAIRS, Material.WARPED_TRAPDOOR, Material.WARPED_WALL_SIGN
    ));

    public static final Set<Material> BREAK = new HashSet<>(Arrays.asList(
        Material.AIR,
        Material.WATER,
        Material.LAVA,
        Material.TALL_GRASS,
        Material.SNOW,
        Material.GRASS_PATH,
        Material.CAVE_AIR,
        Material.VINE,
        Material.FERN,
        Material.LARGE_FERN,
        Material.SUGAR_CANE,
        Material.TWISTING_VINES,
        Material.WEEPING_VINES,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SUNFLOWER,
        Material.FIRE,
        Material.SOUL_FIRE
    ));

    public static final Set<Material> WATER = new HashSet<>(Arrays.asList(
        Material.WATER,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.KELP,
        Material.KELP_PLANT
    ));

    public static final Set<Material> SPAWN = new HashSet<>(Arrays.asList(
        Material.AIR,
        Material.TALL_GRASS,
        Material.SNOW,
        Material.CAVE_AIR,
        Material.VINE,
        Material.FERN,
        Material.LARGE_FERN,
        Material.SUGAR_CANE,
        Material.TWISTING_VINES,
        Material.WEEPING_VINES,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SUNFLOWER,
        Material.FIRE,
        Material.SOUL_FIRE
    ));

    public static final Set<Material> FALL = new HashSet<>(Arrays.asList(
        Material.AIR,
        Material.TALL_GRASS,
        Material.SNOW,
        Material.CAVE_AIR,
        Material.VINE,
        Material.FERN,
        Material.LARGE_FERN,
        Material.SUGAR_CANE,
        Material.TWISTING_VINES,
        Material.WEEPING_VINES,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.KELP,
        Material.KELP_PLANT,
        Material.SUNFLOWER,
        Material.WATER
    ));

    public static final Set<Material> FENCE = new HashSet<>(Arrays.asList(
        Material.OAK_FENCE,
        Material.ACACIA_FENCE,
        Material.BIRCH_FENCE,
        Material.CRIMSON_FENCE,
        Material.DARK_OAK_FENCE,
        Material.JUNGLE_FENCE,
        Material.NETHER_BRICK_FENCE,
        Material.SPRUCE_FENCE,
        Material.WARPED_FENCE,
        Material.COBBLESTONE_WALL,
        Material.ANDESITE_WALL,
        Material.BLACKSTONE_WALL,
        Material.BRICK_WALL,
        Material.GRANITE_WALL,
        Material.DIORITE_WALL,
        Material.SANDSTONE_WALL,
        Material.RED_SANDSTONE_WALL,
        Material.RED_NETHER_BRICK_WALL,
        Material.IRON_BARS,
        Material.COBWEB
    ));

    public static final Set<Material> LEAVES = new HashSet<>(Arrays.asList(
        Material.BIRCH_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.OAK_LEAVES,
        Material.SPRUCE_LEAVES
    ));
}
