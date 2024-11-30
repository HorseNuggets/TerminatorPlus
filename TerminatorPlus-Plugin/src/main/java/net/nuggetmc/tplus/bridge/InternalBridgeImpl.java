package net.nuggetmc.tplus.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.nuggetmc.tplus.api.InternalBridge;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InternalBridgeImpl implements InternalBridge {
    @Override
    public void sendBlockDestructionPacket(short entityId, Block block, int progress) {
        ClientboundBlockDestructionPacket crack = new ClientboundBlockDestructionPacket(entityId, new BlockPos(block.getX(), block.getY(), block.getZ()), progress);
        for (Player all : block.getLocation().getNearbyPlayers(64)) {
            ((CraftPlayer) all).getHandle().connection.send(crack);
        }
    }
}
