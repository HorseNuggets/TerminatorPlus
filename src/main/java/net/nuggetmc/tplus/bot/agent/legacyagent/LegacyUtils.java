package net.nuggetmc.tplus.bot.agent.legacyagent;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class LegacyUtils {

    public static boolean checkFreeSpace(Location a, Location b) {
        Vector v = b.toVector().subtract(a.toVector());

        int n = 32;
        double m = 1 / (double) n;

        double j = Math.floor(v.length() * n);
        v.multiply(m / v.length());

        org.bukkit.World world = a.getWorld();
        if (world == null) return false;

        for (int i = 0; i <= j; i++) {
            Block block = world.getBlockAt((a.toVector().add(v.clone().multiply(i))).toLocation(world));

            if (!LegacyMats.AIR.contains(block.getType())) {
                return false;
            }
        }

        return true;
    }

    public static Sound breakBlockSound(Block block) {
        try {
            World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            net.minecraft.server.v1_16_R3.Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();

            SoundEffectType soundEffectType = nmsBlock.getStepSound(nmsBlock.getBlockData());

            Field breakSound = SoundEffectType.class.getDeclaredField("stepSound");
            breakSound.setAccessible(true);
            SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);

            Field keyField = SoundEffect.class.getDeclaredField("b");
            keyField.setAccessible(true);
            MinecraftKey nmsString = (MinecraftKey) keyField.get(nmsSound);

            return Sound.valueOf(nmsString.getKey().replace(".", "_").toUpperCase());
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            return null;
        }
    }
}
