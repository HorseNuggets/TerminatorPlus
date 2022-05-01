package net.nuggetmc.tplus.bot.agent.legacyagent.ai;

import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.utils.MathUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

// If this is laggy, try only instantiating this once and update it instead of creating a new instance every tick
public class BotData {

    private final Map<BotDataType, Double> values;

    private BotData(Bot bot, LivingEntity target) {
        this.values = new HashMap<>();

        Location a = bot.getLocation();
        Location b = target.getLocation();

        float health = bot.getHealth();

        values.put(BotDataType.CRITICAL_HEALTH, health >= 5 ? 0 : 5D - health);
        values.put(BotDataType.DISTANCE_XZ, Math.sqrt(MathUtils.square(a.getX() - b.getX()) + MathUtils.square(a.getZ() - b.getZ())));
        values.put(BotDataType.DISTANCE_Y, b.getY() - a.getY());
        values.put(BotDataType.ENEMY_BLOCKING, target instanceof Player && ((Player)target).isBlocking() ? 1D : 0);
    }

    public static BotData generate(Bot bot, LivingEntity target) {
        return new BotData(bot, target);
    }

    public Map<BotDataType, Double> getValues() {
        return values;
    }

    public double getValue(BotDataType dataType) {
        return values.get(dataType);
    }

    @Override
    public String toString() {
        List<String> strings = new ArrayList<>();

        values.forEach((type, value) -> strings.add(type.name() + "=" + MathUtils.round2Dec(value)));

        Collections.sort(strings);

        return "BotData{" + StringUtils.join(strings, ",") + "}";
    }
}
