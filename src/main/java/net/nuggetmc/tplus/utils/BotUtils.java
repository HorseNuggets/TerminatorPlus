package net.nuggetmc.tplus.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BotUtils {

    public static final Set<Material> NO_FALL = new HashSet<>(Arrays.asList(
        Material.WATER,
        Material.LAVA,
        Material.TWISTING_VINES,
        Material.VINE
    ));

    public static UUID randomSteveUUID() {
        UUID uuid = UUID.randomUUID();

        if (uuid.hashCode() % 2 != 0)
            return randomSteveUUID();

        return uuid;
    }

    public static boolean solidAt(Location loc) {
        Block block = loc.getBlock();
        BoundingBox box = block.getBoundingBox();
        Vector position = loc.toVector();

        return position.getX() > box.getMinX() && position.getX() < box.getMaxX() && position.getY() > box.getMinY() && position.getY() < box.getMaxY() && position.getZ() > box.getMinZ() && position.getZ() < box.getMaxZ();
    }
}
