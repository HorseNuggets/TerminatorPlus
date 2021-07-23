package net.nuggetmc.ai.bot;

import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.nuggetmc.ai.bot.agent.Agent;
import net.nuggetmc.ai.bot.agent.legacyagent.LegacyAgent;
import net.nuggetmc.ai.bot.agent.legacyagent.ai.NetworkType;
import net.nuggetmc.ai.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.ai.utils.MojangAPI;
import org.bukkit.*;
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

    private final Agent agent;
    private final Set<Bot> bots;
    private final NumberFormat numberFormat;

    public boolean joinMessages = false;
    public boolean removeOnDeath = true;

    public BotManager() {
        this.agent = new LegacyAgent(this);
        this.bots = new HashSet<>();
        this.numberFormat = NumberFormat.getInstance(Locale.US);
    }

    public Set<Bot> fetch() {
        return bots;
    }

    public void add(Bot bot) {
        if (joinMessages) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + bot.getName() + " joined the game");
        }

        bots.add(bot);
    }

    public Bot getFirst(String name) {
        for (Bot bot : bots) {
            if (name.equals(bot.getName())) {
                return bot;
            }
        }

        return null;
    }

    public Agent getAgent() {
        return agent;
    }

    public void createBots(Player sender, String name, String skinName, int n) {
        createBots(sender, name, skinName, n, null);
    }

    public void createBots(Player sender, String name, String skinName, int n, NetworkType type) {
        long timestamp = System.currentTimeMillis();

        if (n < 1) n = 1;

        World world = sender.getWorld();
        Location loc = sender.getLocation();

        sender.sendMessage("Creating " + (n == 1 ? "new bot" : ChatColor.RED + numberFormat.format(n) + ChatColor.RESET + " new bots")
                + " with name " + ChatColor.GREEN + name
                + (skinName == null ? "" : ChatColor.RESET + " and skin " + ChatColor.GREEN + skinName)
                + ChatColor.RESET + "...");

        skinName = skinName == null ? name : skinName;

        double f = n < 100 ? .004 * n : .4;

        String[] skin = MojangAPI.getSkin(skinName);

        for (int i = 1; i <= n; i++) {
            Bot bot = Bot.createBot(loc, name.replace("%", String.valueOf(i)), skin);

            if (i > 1) {
                bot.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5).normalize().multiply(f));
            }

            if (type == NetworkType.RANDOM) {
                bot.setNeuralNetwork(NeuralNetwork.generateRandomNetwork());
            }
        }

        world.spawnParticle(Particle.CLOUD, loc, 100, 1, 1, 1, 0.5);

        sender.sendMessage("Process completed (" + ChatColor.RED + ((System.currentTimeMillis() - timestamp) / 1000D) + "s" + ChatColor.RESET + ").");
    }

    public void remove(Bot bot) {
        bots.remove(bot);
    }

    public void reset() {
        bots.forEach(Bot::removeVisually);
        bots.clear(); // Not always necessary, but a good security measure
        agent.stopAllTasks();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;
        bots.forEach(bot -> bot.render(connection, true));
    }
}
