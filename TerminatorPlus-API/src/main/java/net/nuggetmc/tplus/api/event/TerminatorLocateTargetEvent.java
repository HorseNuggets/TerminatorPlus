package net.nuggetmc.tplus.api.event;

import net.nuggetmc.tplus.api.Terminator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TerminatorLocateTargetEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Terminator terminator;
    private LivingEntity target;
    private boolean cancelled;

    public TerminatorLocateTargetEvent(Terminator terminator, LivingEntity target) {
        this.terminator = terminator;
        this.target = target;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }


    public Terminator getTerminator() {
        return terminator;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
