package net.nuggetmc.ai.bot.agent;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BotAgent {

    private BotManager manager;

    private byte quarterTick = 0;

    public BotAgent(BotManager manager) {
        this.manager = manager;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(PlayerAI.getInstance(), this::tick, 0, 1);
    }

    private void tick() {
        quarterTick = (byte) ((quarterTick + 1) % 5);
        manager.fetch().forEach(this::tickBot);
    }

    private void tickBot(Bot bot) {
        Location loc = bot.getLocation();

        Player player = nearestPlayer(loc);
        if (player == null) return;

        Location target = player.getLocation();
        Vector vel = target.toVector().subtract(loc.toVector()).normalize();

        if (quarterTick == 0) {
            bot.faceLocation(target);
        }

        try {
            vel.checkFinite();
            vel.add(bot.velocity);
        } catch (IllegalArgumentException e) {
            vel = bot.velocity;
        }

        if (vel.length() > 1) vel.normalize();
        vel.multiply(0.3);
        vel.setY(0.5);

        if (bot.predictGround()) {
            bot.jump(vel);
        }
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
