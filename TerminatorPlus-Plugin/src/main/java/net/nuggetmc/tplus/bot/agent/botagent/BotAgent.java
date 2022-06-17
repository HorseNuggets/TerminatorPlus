package net.nuggetmc.tplus.bot.agent.botagent;

import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.bot.agent.Agent;
import net.nuggetmc.tplus.utils.MathUtils;
import net.nuggetmc.tplus.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Set;

/*
 * New bot agent!!!!!
 * this will replace legacyagent eventually
 * - basically this one will actually have A* pathfinding, whereas the legacy one only straightlines
 */

public class BotAgent extends Agent {

    private int count;

    public BotAgent(BotManager manager) {
        super(manager);
    }

    @Override
    protected void tick() {
        Set<Bot> bots = manager.fetch();
        count = bots.size();
        bots.forEach(this::tickBot);
    }

    // This is where the code starts to get spicy
    private void tickBot(Bot bot) {
        if (!bot.isAlive()) return;

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
        if (PlayerUtils.isInvincible(player.getGameMode()) || player.getNoDamageTicks() >= 5 || loc.distance(player.getLocation()) >= 4) return;

        bot.attack(player);
    }

    private void move(Bot bot, Player player, Location loc, Location target) {
        Vector vel = target.toVector().subtract(loc.toVector()).normalize();

        if (bot.tickDelay(5)) bot.faceLocation(player.getLocation());
        if (!bot.isOnGround()) return; // calling this a second time later on

        bot.stand(); // eventually create a memory system so packets do not have to be sent every tick
        bot.setItem(null); // method to check item in main hand, bot.getItemInHand()

        try {
            vel.add(bot.getVelocity());
        } catch (IllegalArgumentException e) {
            if (MathUtils.isNotFinite(vel)) {
                MathUtils.clean(vel);
            }
        }

        if (vel.length() > 1) vel.normalize();

        if (loc.distance(target) <= 5) {
            vel.multiply(0.3);
        } else {
            vel.multiply(0.4);
        }

        vel.setY(0.4);

        bot.jump(vel);
    }

    private Player nearestPlayer(Location loc) {
        Player result = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerUtils.isInvincible(player.getGameMode()) || loc.getWorld() != player.getWorld()) continue;

            if (result == null || loc.distance(player.getLocation()) < loc.distance(result.getLocation())) {
                result = player;
            }
        }

        return result;
    }
}
