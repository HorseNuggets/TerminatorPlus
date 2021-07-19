package net.nuggetmc.ai.bot.event;

import net.nuggetmc.ai.bot.Bot;

public class BotFallDamageEvent {

    private final Bot bot;

    private boolean isCancelled;

    public BotFallDamageEvent(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public void cancel() {
        isCancelled = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}
