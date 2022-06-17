package net.nuggetmc.tplus.bot.event;

import net.nuggetmc.tplus.bot.Bot;
import org.bukkit.event.entity.EntityDeathEvent;

public class BotDeathEvent extends EntityDeathEvent {

    private final Bot bot;

    public BotDeathEvent(EntityDeathEvent event, Bot bot) {
        super(event.getEntity(), event.getDrops(), event.getDroppedExp());
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }
}
