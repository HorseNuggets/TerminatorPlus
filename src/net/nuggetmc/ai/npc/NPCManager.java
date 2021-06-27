package net.nuggetmc.ai.npc;

import io.netty.channel.*;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.nuggetmc.ai.PlayerAI;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NPCManager implements Listener {

    private PlayerAI plugin;

    private final Set<NPC> NPCS = new HashSet<>();
    private final Map<Integer, NPC> NPC_CONNECTIONS = new HashMap<>();

    public Set<NPC> fetch() {
        return NPCS;
    }

    public void add(NPC npc) {
        NPCS.add(npc);

        Bukkit.getScheduler().runTaskLater(PlayerAI.getInstance(), () -> {
            NPC_CONNECTIONS.put(npc.getId(), npc);
        }, 10);
    }

    public NPCManager(PlayerAI instance) {
        plugin = instance;
    }

    public void reset() {
        for (NPC npc : NPCS) {
            npc.despawn();
        }

        NPCS.clear();
        NPC_CONNECTIONS.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;

        for (NPC npc : NPCS) {
            npc.render(connection, true);
        }

        injectPlayer(event.getPlayer());
    }

    public void connectAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            injectPlayer(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    public void disconnectAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removePlayer(player);
        }
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                if (packet instanceof PacketPlayOutNamedEntitySpawn) {
                    renderNPC(player, (PacketPlayOutNamedEntitySpawn) packet);
                }

                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();

        try {
            pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
        } catch (IllegalArgumentException ignore) { }
    }

    private void renderNPC(Player player, PacketPlayOutNamedEntitySpawn packet) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        Field field;

        try {
            field = packet.getClass().getDeclaredField("a");
        } catch (NoSuchFieldException e) {
            return;
        }

        field.setAccessible(true);

        Object obj;

        try {
            obj = field.get(packet);
        } catch (IllegalAccessException e) {
            return;
        }

        if (!(obj instanceof Integer)) return;
        int n = (int) obj;

        NPC npc = NPC_CONNECTIONS.get(n);
        if (npc == null) return;

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));

        PacketPlayOutPlayerInfo noTabPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!connection.isDisconnected()) {
                connection.sendPacket(noTabPacket);
            }
        }, 5);
    }

    public void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }
}
