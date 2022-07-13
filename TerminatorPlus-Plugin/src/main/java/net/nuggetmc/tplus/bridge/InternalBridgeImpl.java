package net.nuggetmc.tplus.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuggetmc.tplus.api.InternalBridge;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InternalBridgeImpl implements InternalBridge {
    @Override
    public void sendBlockDestructionPacket(short entityId, int x, int y, int z, int progress) {
        ClientboundBlockDestructionPacket crack = new ClientboundBlockDestructionPacket(entityId, new BlockPos(x, y, z), progress);
        for (Player all : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) all).getHandle().connection.send(crack);
        }
    }

    @Override
    public Sound breakBlockSound(Block block) {
        Level nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
        BlockState blockState = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ()));
        net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();

        SoundType soundEffectType = nmsBlock.getSoundType(blockState);

        return Sound.valueOf(soundEffectType.getBreakSound().getLocation().getPath().replace(".", "_").toUpperCase());
    }
}
