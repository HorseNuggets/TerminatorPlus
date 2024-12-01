package net.nuggetmc.tplus.api.event;

import net.nuggetmc.tplus.api.Terminator;
import org.bukkit.event.entity.EntityDeathEvent;

public class BotDeathEvent extends EntityDeathEvent {

    private final Terminator bot;

    public BotDeathEvent(EntityDeathEvent event, Terminator bot) {
        super(event.getEntity(), event.getDamageSource(), event.getDrops(), event.getDroppedExp());
        this.bot = bot;
    }

    public Terminator getBot() {
        return bot;
    }
}
