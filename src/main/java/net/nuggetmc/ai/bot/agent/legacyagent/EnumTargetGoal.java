package net.nuggetmc.ai.bot.agent.legacyagent;

public enum EnumTargetGoal { // TODO USE ORDINAL!!!!!
    NEAREST_REAL_VULNERABLE_PLAYER,
    NEAREST_REAL_PLAYER,
    NEAREST_BOT_DIFFER,
    NEAREST_BOT,
    NEAREST_BOT_DIFFER_ALPHA,
    NONE;

    public static EnumTargetGoal of(int n) {
        switch (n) {
            default:
                return NONE;

            case 1:
                return NEAREST_REAL_VULNERABLE_PLAYER;

            case 2:
                return NEAREST_REAL_PLAYER;

            case 3:
                return NEAREST_BOT_DIFFER;

            case 4:
                return NEAREST_BOT;

            case 5:
                return NEAREST_BOT_DIFFER_ALPHA;
        }
    }
}
