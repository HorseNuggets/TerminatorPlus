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

        if (uuid.hashCode() % 2 == 0) {
            return uuid;
        }

        return randomSteveUUID();
    }

    public static boolean solidAt(Location loc) { // not perfect, still cuts corners of fences
        Block block = loc.getBlock();
        BoundingBox box = block.getBoundingBox();
        Vector position = loc.toVector();

        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        double minX = box.getMinX();
        double minY = box.getMinY();
        double minZ = box.getMinZ();

        double maxX = box.getMaxX();
        double maxY = box.getMaxY();
        double maxZ = box.getMaxZ();

        return x > minX && x < maxX && y > minY && y < maxY && z > minZ && z < maxZ;
    }
}
