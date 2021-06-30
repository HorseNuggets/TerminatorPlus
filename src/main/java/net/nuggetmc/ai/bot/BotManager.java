package net.nuggetmc.ai.bot;

import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.agent.BotAgent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BotManager implements Listener {

    private final PlayerAI plugin;
    private final BotAgent agent;
    private final NumberFormat numberFormat;

    private final Set<Bot> bots = new HashSet<>();

    public BotManager(PlayerAI plugin) {
        this.plugin = plugin;
        this.agent = new BotAgent(this);
        this.numberFormat = NumberFormat.getInstance(Locale.US);
    }

    public Set<Bot> fetch() {
        return bots;
    }

    public void add(Bot bot) {
        bots.add(bot);
    }

    public BotAgent getAgent() {
        return agent;
    }

    public void createBots(Player sender, String name, String skin, int n) {
        long timestamp = System.currentTimeMillis();

        if (n < 1) n = 1;

        World world = sender.getWorld();
        Location loc = sender.getLocation();

        if (name.length() > 16) name = name.substring(0, 16);
        if (skin != null && skin.length() > 16) skin = skin.substring(0, 16);

        sender.sendMessage("Creating " + (n == 1 ? "new bot" : ChatColor.RED + numberFormat.format(n) + ChatColor.RESET + " new bots")
                + " with name " + ChatColor.GREEN + name
                + (skin == null ? "" : ChatColor.RESET + " and skin " + ChatColor.GREEN + skin)
                + ChatColor.RESET + "...");

        skin = skin == null ? name : skin;

        double f = n < 100 ? .004 * n : .4;

        for (int i = 0; i < n; i++) {
            Bot bot = Bot.createBot(loc, name, skin);
            if (i > 0) bot.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5).normalize().multiply(f));
        }

        world.spawnParticle(Particle.CLOUD, loc, 100, 1, 1, 1, 0.5);

        sender.sendMessage("Process completed (" + ChatColor.RED + ((System.currentTimeMillis() - timestamp) / 1000D) + "s" + ChatColor.RESET + ").");
    }

    public void reset() {
        for (Bot bot : bots) {
            bot.remove();
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
