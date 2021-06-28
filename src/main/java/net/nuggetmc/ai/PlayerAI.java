package net.nuggetmc.ai;

import net.nuggetmc.ai.commands.CommandHandler;
import net.nuggetmc.ai.bot.BotManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerAI extends JavaPlugin {

    public static final double VERSION = 1.0;

    private static PlayerAI instance;

    private CommandHandler handler;
    private BotManager manager;

    public static PlayerAI getInstance() {
        return instance;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    public BotManager getManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Create Instances
        this.manager = new BotManager(this);
        this.handler = new CommandHandler(this);

        // Register all the things
        this.registerEvents();
    }

    @Override
    public void onDisable() {
        manager.reset();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(manager, this);
    }
}
