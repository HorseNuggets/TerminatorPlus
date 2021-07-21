package net.nuggetmc.ai.bot.agent.legacyagent.ai;

import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.utils.MathUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// If this is laggy, try only instantiating this once and update it instead of creating a new instance every tick
public class BotData {

    private final float health;

    private final double distXZ;
    private final double distY;

    private final boolean enemyBlocking;

    private BotData(Bot bot, Player target) {
        Location a = bot.getLocation();
        Location b = target.getLocation();

        this.health = bot.getHealth();
        this.distXZ = Math.sqrt(MathUtils.square(a.getX() - b.getX()) + MathUtils.square(a.getZ() - b.getZ()));
        this.distY = b.getY() - a.getY();
        this.enemyBlocking = target.isBlocking();
    }

    public static BotData generate(Bot bot, Player target) {
        return new BotData(bot, target);
    }

    public float getHealth() {
        return health;
    }

    public double getDistXZ() {
        return distXZ;
    }

    public double getDistY() {
        return distY;
    }

    public boolean getEnemyBlocking() {
        return enemyBlocking;
    }
}
