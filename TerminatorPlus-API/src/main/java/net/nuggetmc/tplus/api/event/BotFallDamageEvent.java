package net.nuggetmc.tplus.api.event;

import java.util.List;

import org.bukkit.block.Block;

import net.nuggetmc.tplus.api.Terminator;

public class BotFallDamageEvent {

    private final Terminator bot;
    private final List<Block> standingOn;

    private boolean cancelled;

    public BotFallDamageEvent(Terminator bot, List<Block> standingOn) {
        this.bot = bot;
        this.standingOn = standingOn;
    }

    public Terminator getBot() {
        return bot;
    }

    public List<Block> getStandingOn() {
        return standingOn;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
