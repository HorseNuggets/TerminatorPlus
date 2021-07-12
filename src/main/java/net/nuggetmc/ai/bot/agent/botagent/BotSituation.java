package net.nuggetmc.ai.bot.agent.botagent;

import net.nuggetmc.ai.bot.Bot;
import org.bukkit.Location;

public class BotSituation {

    private final VerticalDisplacement disp;

    /*
     * aboveGround
     */

    public BotSituation(Bot bot, Location target) {
        Location loc = bot.getLocation();

        this.disp = VerticalDisplacement.fetch(loc.getBlockY(), target.getBlockY());
    }

    public VerticalDisplacement getVerticalDisplacement() {
        return disp;
    }
}
