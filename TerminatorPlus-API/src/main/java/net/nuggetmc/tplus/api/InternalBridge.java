package net.nuggetmc.tplus.api;

import org.bukkit.Sound;
import org.bukkit.block.Block;

/**
 * This class serves as a bridge between the API and internal code that interacts with NMS.
 */
public interface InternalBridge {
    void sendBlockDestructionPacket(short entityId, int x, int y, int z, int progress);

    Sound breakBlockSound(Block block);
}
