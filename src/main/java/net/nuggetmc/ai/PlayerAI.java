package net.nuggetmc.ai;

import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.command.CommandHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PlayerAI extends JavaPlugin {

    private static PlayerAI instance;
    private static String version;

    private BotManager manager;

    public static PlayerAI getInstance() {
        return instance;
    }

    public static String getVersion() {
        return version;
    }

    public BotManager getManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        instance = this;
        version = this.getDescription().getVersion();

        // Create Instances
        this.manager = new BotManager();
        new CommandHandler(this);

        // Register event listeners
        this.registerEvents(manager);
    }

    @Override
    public void onDisable() {
        manager.reset();
    }

    private void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(li -> this.getServer().getPluginManager().registerEvents(li, this));
    }
}
