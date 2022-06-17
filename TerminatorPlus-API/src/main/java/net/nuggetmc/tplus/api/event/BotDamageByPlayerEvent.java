package net.nuggetmc.tplus.api.event;

import net.nuggetmc.tplus.api.Terminator;
import org.bukkit.entity.Player;

public class BotDamageByPlayerEvent {

    private final Terminator bot;
    private final Player player;

    private float damage;

    private boolean cancelled;

    public BotDamageByPlayerEvent(Terminator bot, Player player, float damage) {
        this.bot = bot;
        this.player = player;
        this.damage = damage;
    }

    public Terminator getBot() {
        return bot;
    }

    public Player getPlayer() {
        return player;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
