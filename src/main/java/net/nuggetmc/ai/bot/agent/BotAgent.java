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

    private final PlayerAI plugin;
    private final BotManager manager;
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

    // This is where the code starts to get spicy
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

        BotSituation situation = new BotSituation(bot, target);

        // based on the situation, the bot can perform different actions
        // there can be priorities assigned

        // for building up, bot.setAction(BotAction.TOWER) or bot.startBuildingUp()

        VerticalDisplacement disp = situation.getVerticalDisplacement();

        // Later on maybe do bot.setAction(Action.MOVE) and what not instead of hardcoding it here

        // bot.setSneaking(false);
        move(bot, player, loc, target);
        /*if (disp == VerticalDisplacement.ABOVE) {
            if (bot.isOnGround()) { // checks this twice, again during .jump()
                bot.sneak();
                bot.look(BlockFace.DOWN);
                bot.jump();
                // bot.setSneaking(true);

                // delay -> block place underneath and .setSneaking(false) << check possibilities of cancelling (add a cancel system)
                // catch exceptions for slabs
                scheduler.runTaskLater(plugin, () -> {
                    if (bot.isAlive()) {
                        bot.setItem(new ItemStack(Material.COBBLESTONE));
                        bot.attemptBlockPlace(loc, Material.COBBLESTONE);
                    }
                }, 6);

            } // maybe they will be in water or something, do not make them just do nothing here
        } else {
            move(bot, player, loc, target);
        }*/

        if (bot.tickDelay(3)) attack(bot, player, loc);
    }

    private void attack(Bot bot, Player player, Location loc) {
        if (!PlayerUtils.isVulnerableGameMode(player.getGameMode()) || player.getNoDamageTicks() >= 5 || loc.distance(player.getLocation()) >= 4) return;

        bot.attack(player);
    }

    private void move(Bot bot, Player player, Location loc, Location target) {
        Vector vel = target.toVector().subtract(loc.toVector()).normalize();

        if (bot.tickDelay(5)) bot.faceLocation(player.getLocation());
        if (!bot.isOnGround()) return; // calling this a second time later on

        bot.stand(); // eventually create a memory system so packets do not have to be sent every tick
        bot.setItem(null); // method to check item in main hand, bot.getItemInHand()

        try {
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
