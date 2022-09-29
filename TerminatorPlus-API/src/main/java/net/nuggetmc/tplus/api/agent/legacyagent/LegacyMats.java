package net.nuggetmc.tplus.api.agent.legacyagent;

import org.bukkit.Material;
import org.bukkit.block.data.type.*;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LegacyMats {

    public static final Set<Material> AIR = new HashSet<>(Arrays.asList(
        Material.WATER,
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
        Material.VOID_AIR,
        Material.AIR,
        Material.SOUL_FIRE
    ));

    public static final Set<Material> SHOVEL = new HashSet<>(concatTypes(Lists.newArrayList(
    	Material.CLAY,
        Material.DIRT,
        Material.GRASS_BLOCK,
        Material.COARSE_DIRT,
        Material.PODZOL,
        Material.MYCELIUM,
        Material.GRAVEL,
        Material.MUD,
        Material.MUDDY_MANGROVE_ROOTS,
        Material.SAND,
        Material.RED_SAND,
        Material.SOUL_SAND,
        Material.SOUL_SOIL,
        Material.SNOW,
        Material.SNOW_BLOCK
    ), Arrays.asList(), Arrays.asList(), m -> m.name().endsWith("_CONCRETE_POWDER")));

    public static final Set<Material> AXE = new HashSet<>(Arrays.asList(
        Material.OAK_PLANKS, Material.OAK_DOOR, Material.OAK_FENCE, Material.OAK_FENCE_GATE, Material.OAK_LOG,
        Material.OAK_SIGN, Material.OAK_SLAB, Material.OAK_STAIRS, Material.OAK_TRAPDOOR, Material.OAK_WALL_SIGN, Material.OAK_WOOD,
        Material.DARK_OAK_PLANKS, Material.DARK_OAK_DOOR, Material.DARK_OAK_FENCE, Material.DARK_OAK_FENCE_GATE, Material.DARK_OAK_LOG,
        Material.DARK_OAK_SIGN, Material.DARK_OAK_SLAB, Material.DARK_OAK_STAIRS, Material.DARK_OAK_TRAPDOOR, Material.DARK_OAK_WALL_SIGN, Material.DARK_OAK_WOOD,
        Material.ACACIA_PLANKS, Material.ACACIA_DOOR, Material.ACACIA_FENCE, Material.ACACIA_FENCE_GATE, Material.ACACIA_LOG,
        Material.ACACIA_SIGN, Material.ACACIA_SLAB, Material.ACACIA_STAIRS, Material.ACACIA_TRAPDOOR, Material.ACACIA_WALL_SIGN, Material.ACACIA_WOOD,
        Material.BIRCH_PLANKS, Material.BIRCH_DOOR, Material.BIRCH_FENCE, Material.BIRCH_FENCE_GATE, Material.BIRCH_LOG,
        Material.BIRCH_SIGN, Material.BIRCH_SLAB, Material.BIRCH_STAIRS, Material.BIRCH_TRAPDOOR, Material.BIRCH_WALL_SIGN, Material.BIRCH_WOOD,
        Material.JUNGLE_PLANKS, Material.JUNGLE_DOOR, Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE, Material.JUNGLE_LOG,
        Material.JUNGLE_SIGN, Material.JUNGLE_SLAB, Material.JUNGLE_STAIRS, Material.JUNGLE_TRAPDOOR, Material.JUNGLE_WALL_SIGN, Material.JUNGLE_WOOD,
        Material.SPRUCE_PLANKS, Material.SPRUCE_DOOR, Material.SPRUCE_FENCE, Material.SPRUCE_FENCE_GATE, Material.SPRUCE_LOG,
        Material.SPRUCE_SIGN, Material.SPRUCE_SLAB, Material.SPRUCE_STAIRS, Material.SPRUCE_TRAPDOOR, Material.SPRUCE_WALL_SIGN, Material.SPRUCE_WOOD,
        Material.MANGROVE_PLANKS, Material.MANGROVE_DOOR, Material.MANGROVE_FENCE, Material.MANGROVE_FENCE_GATE, Material.MANGROVE_LOG,
        Material.MANGROVE_SIGN, Material.MANGROVE_SLAB, Material.MANGROVE_STAIRS, Material.MANGROVE_TRAPDOOR, Material.MANGROVE_WALL_SIGN, Material.MANGROVE_WOOD,
        Material.CRIMSON_PLANKS, Material.CRIMSON_DOOR, Material.CRIMSON_FENCE, Material.CRIMSON_FENCE_GATE, Material.CRIMSON_STEM,
        Material.CRIMSON_SIGN, Material.CRIMSON_SLAB, Material.CRIMSON_STAIRS, Material.CRIMSON_TRAPDOOR, Material.CRIMSON_WALL_SIGN,
        Material.WARPED_PLANKS, Material.WARPED_DOOR, Material.WARPED_FENCE, Material.WARPED_FENCE_GATE, Material.WARPED_STEM,
        Material.WARPED_SIGN, Material.WARPED_SLAB, Material.WARPED_STAIRS, Material.WARPED_TRAPDOOR, Material.WARPED_WALL_SIGN,
        Material.CHEST, Material.TRAPPED_CHEST
    ));

    public static final Set<Material> BREAK = new HashSet<>(Arrays.asList(
        Material.AIR,
        Material.WATER,
        Material.LAVA,
        Material.TALL_GRASS,
        Material.SNOW,
        Material.CAVE_AIR,
        Material.VINE,
        Material.FERN,
        Material.LARGE_FERN,
        Material.SUGAR_CANE,
        Material.TWISTING_VINES,
        Material.TWISTING_VINES_PLANT,
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

    public static final Set<Material> FENCE = new HashSet<>(concatTypes(new ArrayList<>(),
    	Arrays.asList(Material.GLASS_PANE, Material.IRON_BARS), Arrays.asList(Fence.class, Wall.class)));
    
    public static final Set<Material> GATES = new HashSet<>(concatTypes(Gate.class));
    
    public static final Set<Material> OBSTACLES = new HashSet<>(concatTypes(Lists.newArrayList(
    	Material.IRON_BARS,
    	Material.CHAIN,
    	Material.END_ROD,
    	Material.LIGHTNING_ROD,
    	Material.COBWEB,
    	Material.SWEET_BERRY_BUSH,
    	Material.FLOWER_POT,
    	Material.GLASS_PANE
    ), Arrays.asList(), Arrays.asList(GlassPane.class), m -> m.name().startsWith("POTTED_")));

    //Notice: We exclude blocks that cannot exist without a solid block below (such as rails or crops)
    public static final Set<Material> NONSOLID = new HashSet<>(concatTypes(Lists.newArrayList(
    	Material.COBWEB,
    	Material.END_GATEWAY,
    	Material.END_PORTAL,
    	Material.NETHER_PORTAL,
    	Material.CAVE_VINES_PLANT,
    	Material.GLOW_LICHEN,
    	Material.HANGING_ROOTS,
    	Material.POWDER_SNOW,
    	Material.SCULK_VEIN,
    	Material.STRING,
    	Material.TRIPWIRE_HOOK,
    	Material.LADDER,
    	Material.VINE,
    	Material.SOUL_WALL_TORCH,
    	Material.REDSTONE_WALL_TORCH,
    	Material.WALL_TORCH,
    	Material.WEEPING_VINES_PLANT,
    	Material.WEEPING_VINES,
    	Material.CAVE_VINES_PLANT,
    	Material.CAVE_VINES
    ), Arrays.asList(), Arrays.asList(Switch.class, CoralWallFan.class, WallSign.class), m -> m.name().endsWith("_WALL_BANNER")));
    
    public static final Set<Material> LEAVES = new HashSet<>(concatTypes(Leaves.class));
    
    public static final Set<Material> INSTANT_BREAK = new HashSet<>(concatTypes(Lists.newArrayList(
        Material.TALL_GRASS,
        Material.GRASS,
        Material.FERN,
        Material.LARGE_FERN,
        Material.KELP_PLANT,
        Material.DEAD_BUSH,
        Material.WHEAT_SEEDS,
        Material.POTATOES,
        Material.CARROTS,
        Material.BEETROOT_SEEDS,
        Material.PUMPKIN_STEM,
        Material.MELON_STEM,
        Material.SUGAR_CANE,
        Material.SWEET_BERRY_BUSH,
        Material.LILY_PAD,
        Material.DANDELION,
        Material.POPPY,
        Material.BLUE_ORCHID,
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.RED_TULIP,
        Material.ORANGE_TULIP,
        Material.WHITE_TULIP,
        Material.PINK_TULIP,
        Material.OXEYE_DAISY,
        Material.CORNFLOWER,
        Material.LILY_OF_THE_VALLEY,
        Material.WITHER_ROSE,
        Material.SUNFLOWER,
        Material.LILAC,
        Material.ROSE_BUSH,
        Material.PEONY,
        Material.NETHER_WART,
        Material.FLOWER_POT,
        Material.AZALEA,
        Material.FLOWERING_AZALEA,
        Material.REPEATER,
        Material.COMPARATOR,
        Material.REDSTONE_WIRE,
        Material.REDSTONE_TORCH,
        Material.REDSTONE_WALL_TORCH,
        Material.TORCH,
        Material.WALL_TORCH,
        Material.SOUL_TORCH,
        Material.SOUL_WALL_TORCH,
        Material.SCAFFOLDING,
        Material.SLIME_BLOCK,
        Material.HONEY_BLOCK,
        Material.TNT,
        Material.TRIPWIRE,
        Material.TRIPWIRE_HOOK,
        Material.SPORE_BLOSSOM,
        Material.RED_MUSHROOM,
        Material.BROWN_MUSHROOM,
        Material.CRIMSON_FUNGUS,
        Material.WARPED_FUNGUS,
        Material.CRIMSON_ROOTS,
        Material.WARPED_ROOTS,
        Material.HANGING_ROOTS,
        Material.WEEPING_VINES,
        Material.WEEPING_VINES_PLANT,
        Material.TWISTING_VINES,
        Material.TWISTING_VINES_PLANT,
        Material.CAVE_VINES,
        Material.CAVE_VINES_PLANT,
        Material.SEA_PICKLE
    ), Arrays.asList(), Arrays.asList(Sapling.class, CoralWallFan.class), m -> m.name().endsWith("_CORAL_FAN") || m.name().endsWith("_CORAL")
    	|| m.name().startsWith("POTTED_")));
    
    private static List<Material> concatTypes(Class<?>... types) {
    	return concatTypes(new ArrayList<>(), Arrays.asList(types));
    }
    
    private static List<Material> concatTypes(List<Material> materials, List<Class<?>> types) {
    	return concatTypes(materials, Arrays.asList(), types);
    }
    
    private static List<Material> concatTypes(List<Material> materials, List<Material> exclusions, List<Class<?>> types) {
    	return concatTypes(materials, exclusions, types, m -> false);
    }
    
    private static List<Material> concatTypes(List<Material> materials, List<Material> exclusions, List<Class<?>> types, Predicate<Material> otherFilter) {
    	materials.addAll(Stream.of(Material.values()).filter(m -> (types.contains(m.data) || otherFilter.test(m))
    		&& !exclusions.contains(m) && !m.isLegacy()).toList());
    	return materials;
    }
}
