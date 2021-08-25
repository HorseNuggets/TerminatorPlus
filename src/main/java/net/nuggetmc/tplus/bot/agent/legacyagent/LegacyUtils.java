package net.nuggetmc.tplus.bot.agent.legacyagent;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
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
            Level nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            net.minecraft.world.level.block.Block nmsBlock = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ())).getBlock();

            SoundType soundEffectType = nmsBlock.getSoundType(nmsBlock.defaultBlockState());

            Field breakSound = SoundType.class.getDeclaredField("stepSound");
            breakSound.setAccessible(true);
            SoundEvent nmsSound = (SoundEvent) breakSound.get(soundEffectType);

            Field keyField = SoundEvent.class.getDeclaredField("location");
            keyField.setAccessible(true);
            ResourceLocation nmsString = (ResourceLocation) keyField.get(nmsSound);

            return Sound.valueOf(nmsString.getPath().replace(".", "_").toUpperCase());
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            return null;
        }
    }
}
