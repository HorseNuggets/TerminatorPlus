package net.nuggetmc.tplus.bot.agent.legacyagent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.util.Vector;

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
        Level nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        BlockState blockState = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ()));
        net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();

        SoundType soundEffectType = nmsBlock.getSoundType(blockState);

        return Sound.valueOf( soundEffectType.getBreakSound().getLocation().getPath().replace(".", "_").toUpperCase());
    }
}
