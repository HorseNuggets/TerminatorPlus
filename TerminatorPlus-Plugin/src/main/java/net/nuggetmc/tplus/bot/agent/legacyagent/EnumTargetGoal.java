package net.nuggetmc.tplus.bot.agent.legacyagent;

import java.util.HashMap;
import java.util.Map;

public enum EnumTargetGoal {
    NEAREST_VULNERABLE_PLAYER("Locate the nearest real player that is in either Survival or Adventure mode."),
    NEAREST_PLAYER("Locate the nearest real online player, despite the gamemode."),
    NEAREST_HOSTILE("Locate the nearest hostile entity."),
    NEAREST_MOB("Locate the nearest mob."),
    NEAREST_BOT("Locate the nearest bot."),
    NEAREST_BOT_DIFFER("Locate the nearest bot with a different username."),
    NEAREST_BOT_DIFFER_ALPHA("Locate the nearest bot with a different username after filtering out non-alpha characters."),
    NONE("No target goal.");

    private static final Map<String, EnumTargetGoal> VALUES = new HashMap<String, EnumTargetGoal>() {
        {
            this.put("none", NONE);
            this.put("nearestvulnerableplayer", NEAREST_VULNERABLE_PLAYER);
            this.put("nearestplayer", NEAREST_PLAYER);
            this.put("nearesthostile", NEAREST_HOSTILE);
            this.put("nearestmob", NEAREST_MOB);
            this.put("nearestbot", NEAREST_BOT);
            this.put("nearestbotdiffer", NEAREST_BOT_DIFFER);
            this.put("nearestbotdifferalpha", NEAREST_BOT_DIFFER_ALPHA);
        }
    };

    private final String description;

    EnumTargetGoal(String description) {
        this.description = description;
    }

    public static EnumTargetGoal from(String name) {
        return VALUES.get(name);
    }

    public String description() {
        return description;
    }
}
