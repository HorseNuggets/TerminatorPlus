package net.nuggetmc.tplus.api.agent;

import net.nuggetmc.tplus.api.BotManager;
import net.nuggetmc.tplus.api.Terminator;
import net.nuggetmc.tplus.api.event.BotDamageByPlayerEvent;
import net.nuggetmc.tplus.api.event.BotDeathEvent;
import net.nuggetmc.tplus.api.event.BotFallDamageEvent;
import net.nuggetmc.tplus.api.event.BotKilledByPlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class Agent {

    protected final Plugin plugin;
    protected final BotManager manager;
    protected final BukkitScheduler scheduler;
    protected final Set<BukkitRunnable> taskList;
    protected final Random random;

    protected boolean enabled;
    protected int taskID;

    protected boolean drops;

    public Agent(BotManager manager, Plugin plugin) {
        this.plugin = plugin;
        this.manager = manager;
        this.scheduler = Bukkit.getScheduler();
        this.taskList = new HashSet<>();
        this.random = new Random();

        setEnabled(true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean b) {
        enabled = b;

        if (b) {
            taskID = scheduler.scheduleSyncRepeatingTask(plugin, this::tick, 0, 1);
        } else {
            scheduler.cancelTask(taskID);
            stopAllTasks();
        }
    }

    public void stopAllTasks() {
        if (!taskList.isEmpty()) {
            taskList.stream().filter(t -> !t.isCancelled()).forEach(BukkitRunnable::cancel);
            taskList.clear();
        }
    }

    public void setDrops(boolean enabled) {
        this.drops = enabled;
    }

    protected abstract void tick();

    public void onFallDamage(BotFallDamageEvent event) {
    }

    public void onPlayerDamage(BotDamageByPlayerEvent event) {
    }

    public void onBotDeath(BotDeathEvent event) {
    }

    public void onBotKilledByPlayer(BotKilledByPlayerEvent event) {
        Player player = event.getPlayer();

        scheduler.runTaskAsynchronously(plugin, () -> {
            Terminator bot = manager.getBot(player);

            if (bot != null) {
                bot.incrementKills();
            }
        });
    }
}
