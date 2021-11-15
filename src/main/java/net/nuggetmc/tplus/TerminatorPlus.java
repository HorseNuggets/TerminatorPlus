package net.nuggetmc.tplus;

import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.ui.menu.MenuListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class TerminatorPlus extends JavaPlugin {

    private static TerminatorPlus instance;
    private static String version;

    private BotManager manager;
    private CommandHandler handler;

    public static TerminatorPlus getInstance() {
        return instance;
    }

    public static String getVersion() {
        return version;
    }

    public BotManager getManager() {
        return manager;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    @Override
    public void onEnable() {
        instance = this;
        version = getDescription().getVersion();

        // Create Instances
        this.manager = new BotManager();
        this.handler = new CommandHandler(this);

        // Register event listeners
        this.registerEvents(manager,new MenuListener());
    }

    @Override
    public void onDisable() {
        manager.reset();
    }

    private void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(li -> this.getServer().getPluginManager().registerEvents(li, this));
    }
}
