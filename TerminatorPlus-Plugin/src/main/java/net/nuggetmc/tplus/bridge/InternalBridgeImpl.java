package net.nuggetmc.tplus.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuggetmc.tplus.api.InternalBridge;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InternalBridgeImpl implements InternalBridge {
    @Override
    public void sendBlockDestructionPacket(short entityId, Block block, int progress) {
        ClientboundBlockDestructionPacket crack = new ClientboundBlockDestructionPacket(entityId, new BlockPos(block.getX(), block.getY(), block.getZ()), progress);
        for (Player all : block.getLocation().getNearbyPlayers(64)) {
            ((CraftPlayer) all).getHandle().connection.send(crack);
        }
    }

    @Override
    public Sound breakBlockSound(Block block) {
        try {
            Level nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
            BlockState blockState = nmsWorld.getBlockState(new BlockPos(block.getX(), block.getY(), block.getZ()));
            net.minecraft.world.level.block.Block nmsBlock = blockState.getBlock();

            SoundType soundEffectType = nmsBlock.getSoundType(blockState);

            return Sound.valueOf(soundEffectType.getBreakSound().getLocation().getPath().replace(".", "_").toUpperCase());
        } catch (Exception e) { // potentially unsafe, just fallback to stone break sound
            return Sound.BLOCK_STONE_BREAK;
        }
    }
}
