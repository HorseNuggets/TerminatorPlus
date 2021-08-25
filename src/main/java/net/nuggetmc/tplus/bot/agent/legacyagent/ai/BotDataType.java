package net.nuggetmc.tplus.bot.agent.legacyagent.ai;

public enum BotDataType {
    CRITICAL_HEALTH("h"),
    DISTANCE_XZ("xz"),
    DISTANCE_Y("y"),
    ENEMY_BLOCKING("b");

    private final String shorthand;

    BotDataType(String shorthand) {
        this.shorthand = shorthand;
    }

    public String getShorthand() {
        return shorthand;
    }
}
