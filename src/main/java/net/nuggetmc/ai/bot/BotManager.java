package net.nuggetmc.ai.bot;

import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.nuggetmc.ai.PlayerAI;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

public class BotManager implements Listener {

    private final PlayerAI plugin;

    private final Set<Bot> bots = new HashSet<>();

    public Set<Bot> fetch() {
        return bots;
    }

    public void add(Bot bot) {
        bots.add(bot);
    }

    public BotManager(PlayerAI plugin) {
        this.plugin = plugin;
    }

    public void reset() {
        for (Bot bot : bots) {
            bot.despawn();
        }

        bots.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;

        for (Bot bot : bots) {
            bot.render(connection, true);
        }
    }

}
