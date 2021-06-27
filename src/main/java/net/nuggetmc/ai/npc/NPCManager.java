package net.nuggetmc.ai.npc;

import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.nuggetmc.ai.PlayerAI;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

public class NPCManager implements Listener {

    private final PlayerAI plugin;

    private final Set<NPC> npcs = new HashSet<>();

    public Set<NPC> fetch() {
        return npcs;
    }

    public void add(NPC npc) {
        npcs.add(npc);
    }

    public NPCManager(PlayerAI plugin) {
        this.plugin = plugin;
    }

    public void reset() {
        for (NPC npc : npcs) {
            npc.despawn();
        }

        npcs.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;

        for (NPC npc : npcs) {
            npc.render(connection, true);
        }
    }

}
