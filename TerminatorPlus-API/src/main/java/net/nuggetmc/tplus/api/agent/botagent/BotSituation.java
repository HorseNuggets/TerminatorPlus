package net.nuggetmc.tplus.api.agent.botagent;

import net.nuggetmc.tplus.api.Terminator;
import org.bukkit.Location;

public class BotSituation {

    private final VerticalDisplacement disp;

    /*
     * aboveGround
     */

    public BotSituation(Terminator bot, Location target) {
        Location loc = bot.getLocation();

        this.disp = VerticalDisplacement.fetch(loc.getBlockY(), target.getBlockY());
    }

    public VerticalDisplacement getVerticalDisplacement() {
        return disp;
    }
}
