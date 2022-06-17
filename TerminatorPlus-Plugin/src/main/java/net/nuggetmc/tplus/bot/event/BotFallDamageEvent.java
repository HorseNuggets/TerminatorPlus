package net.nuggetmc.tplus.bot.event;

import net.nuggetmc.tplus.bot.Bot;

public class BotFallDamageEvent {

    private final Bot bot;

    private boolean cancelled;

    public BotFallDamageEvent(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
