package net.nuggetmc.ai.bot.agent;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BotAgent {

    private PlayerAI plugin;
    private BotManager manager;

    public BotAgent(BotManager manager) {
        this.plugin = PlayerAI.getInstance();
        this.manager = manager;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0, 1);
    }

    private void tick() {
        manager.fetch().forEach(this::tickBot);
    }

    private void tickBot(Bot bot) {
        Location loc = bot.getLocation();

        Player player = nearestPlayer(loc);
        if (player == null) return;

        Location target = player.getLocation();

        if (manager.fetch().size() > 1) {
            target.add(bot.getOffset());
        }

        // Make the XZ offsets stored in the bot object (so they don't form a straight line),
        // and make it so when mining and stuff, the offset is not taken into account

        if (bot.tickDelay(3)) attack(bot, player, loc);
        move(bot, player, loc, target);
    }

    private void attack(Bot bot, Player player, Location loc) {
        if (!PlayerUtils.isVulnerableGamemode(player.getGameMode())
                || player.getNoDamageTicks() >= 5
                || loc.distance(player.getLocation()) >= 4) return;

        bot.attack(player);
    }

    private void move(Bot bot, Player player, Location loc, Location target) {
        Vector vel = target.toVector().subtract(loc.toVector()).normalize();

        if (bot.tickDelay(5)) bot.faceLocation(player.getLocation());

        try {
            vel.checkFinite();
            vel.add(bot.velocity);
        } catch (IllegalArgumentException e) {
            vel = bot.velocity;
        }

        if (vel.length() > 1) vel.normalize();
        vel.multiply(0.4).setY(0.4);

        bot.jump(vel);
    }

    private Player nearestPlayer(Location loc) {
        Player result = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (loc.getWorld() != player.getWorld()) continue;

            if (result == null || loc.distance(player.getLocation()) < loc.distance(result.getLocation())) {
                result = player;
            }
        }

        return result;
    }
}
