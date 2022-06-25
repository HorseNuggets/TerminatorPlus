package net.nuggetmc.tplus.api.event;

import net.nuggetmc.tplus.api.Terminator;

public class BotFallDamageEvent {

    private final Terminator bot;

    private boolean cancelled;

    public BotFallDamageEvent(Terminator bot) {
        this.bot = bot;
    }

    public Terminator getBot() {
        return bot;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
