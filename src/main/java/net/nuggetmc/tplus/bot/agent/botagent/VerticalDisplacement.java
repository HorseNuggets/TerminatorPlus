package net.nuggetmc.tplus.bot.agent.botagent;

public enum VerticalDisplacement {
    AT,
    ABOVE,
    BELOW;

    public static VerticalDisplacement fetch(int botY, int targetY) {
        int diff = botY - targetY;

        if (diff >= 2) return BELOW;
        if (diff <= -2) return ABOVE;

        return AT;
    }
}
