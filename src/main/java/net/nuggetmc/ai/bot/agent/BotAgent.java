package net.nuggetmc.ai.bot.agent;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.utils.MathUtils;
import net.nuggetmc.ai.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.Set;

public class BotAgent {

    private PlayerAI plugin;
    private BotManager manager;

    private final BukkitScheduler scheduler;

    private boolean enabled;

    private int taskID;

    private int count;

    public BotAgent(BotManager manager) {
        this.plugin = PlayerAI.getInstance();
        this.manager = manager;
        this.scheduler = Bukkit.getScheduler();

        setEnabled(true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean b) {
        enabled = b;

        if (b) {
            taskID = scheduler.scheduleSyncRepeatingTask(plugin, this::tick, 0, 1);
        } else {
            scheduler.cancelTask(taskID);
        }
    }

    private void tick() {
        Set<Bot> bots = manager.fetch();
        count = bots.size();
        bots.forEach(this::tickBot);
    }

    private void tickBot(Bot bot) {
        Location loc = bot.getLocation();

        // if bot.hasHoldState() return; << This will be to check if a bot is mining or something similar where it can't move

        Player player = nearestPlayer(loc);
        if (player == null) return;

        Location target = player.getLocation();

        if (count > 1) target.add(bot.getOffset());

        // Make the XZ offsets stored in the bot object (so they don't form a straight line),
        // and make it so when mining and stuff, the offset is not taken into account

        // if checkVertical(bot) { break block action add; return; }

        // BotSituation situation = BotSituation.create(bot);

        // based on the situation, the bot can perform different actions
        // there can be priorities assigned

        if (bot.tickDelay(3)) attack(bot, player, loc);
        move(bot, player, loc, target);
    }

    private void attack(Bot bot, Player player, Location loc) {
        if (player.getNoDamageTicks() >= 5 || loc.distance(player.getLocation()) >= 4) return;

        bot.attack(player);
    }

    private void move(Bot bot, Player player, Location loc, Location target) {
        Vector vel = target.toVector().subtract(loc.toVector()).normalize();

        if (bot.tickDelay(5)) bot.faceLocation(player.getLocation());

        try {
            vel.checkFinite();
            vel.add(bot.velocity);
        } catch (IllegalArgumentException e) {
            if (!MathUtils.isFinite(vel)) {
                MathUtils.clean(vel);
            }
        }

        if (vel.length() > 1) vel.normalize();
        vel.multiply(0.4).setY(0.4);

        bot.jump(vel);
    }

    private Player nearestPlayer(Location loc) {
        Player result = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!PlayerUtils.isTargetable(player.getGameMode()) || loc.getWorld() != player.getWorld()) continue;

            if (result == null || loc.distance(player.getLocation()) < loc.distance(result.getLocation())) {
                result = player;
            }
        }

        return result;
    }
}
