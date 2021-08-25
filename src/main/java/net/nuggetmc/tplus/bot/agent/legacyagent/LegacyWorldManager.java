package net.nuggetmc.tplus.bot.agent.legacyagent;

import org.bukkit.Location;
import org.bukkit.Material;

public class LegacyWorldManager {

    /*
     * This is where the respawning queue will be managed
     */

    public static boolean aboveGround(Location loc) {
        int y = 1;

        while (y < 25) {
            if (loc.clone().add(0, y, 0).getBlock().getType() != Material.AIR) {
                return false;
            }

            y++;
        }

        return true;
    }
}
