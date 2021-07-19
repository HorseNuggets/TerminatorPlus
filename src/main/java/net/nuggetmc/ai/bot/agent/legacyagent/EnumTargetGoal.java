package net.nuggetmc.ai.bot.agent.legacyagent;

public enum EnumTargetGoal {
    CLOSEST_REAL_VULNERABLE_PLAYER,
    CLOSEST_REAL_PLAYER,
    CLOSEST_BOT_DIFFER,
    CLOSEST_BOT,
    NONE;

    public static EnumTargetGoal of(int n) {
        switch (n) {
            default:
                return NONE;

            case 1:
                return CLOSEST_REAL_VULNERABLE_PLAYER;

            case 2:
                return CLOSEST_REAL_PLAYER;

            case 3:
                return CLOSEST_BOT_DIFFER;

            case 4:
                return CLOSEST_BOT;
        }
    }
}
