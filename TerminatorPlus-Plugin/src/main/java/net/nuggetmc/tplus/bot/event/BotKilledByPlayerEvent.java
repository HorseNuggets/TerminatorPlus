package net.nuggetmc.tplus.bot.event;

import net.nuggetmc.tplus.bot.Bot;
import org.bukkit.entity.Player;

public class BotKilledByPlayerEvent {

    // eventually also call this event for deaths from other damage causes within combat time
    // (like hitting the ground too hard)

    private final Bot bot;
    private final Player player;

    public BotKilledByPlayerEvent(Bot bot, Player player) {
        this.bot = bot;
        this.player = player;
    }

    public Bot getBot() {
        return bot;
    }

    public Player getPlayer() {
        return player;
    }
}
