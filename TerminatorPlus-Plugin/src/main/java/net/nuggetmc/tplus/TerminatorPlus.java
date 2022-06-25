package net.nuggetmc.tplus;

import net.nuggetmc.tplus.api.TerminatorPlusAPI;
import net.nuggetmc.tplus.bot.BotManagerImpl;
import net.nuggetmc.tplus.bridge.InternalBridgeImpl;
import net.nuggetmc.tplus.command.CommandHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class TerminatorPlus extends JavaPlugin {

    private static TerminatorPlus instance;
    private static String version;

    private BotManagerImpl manager;
    private CommandHandler handler;

    public static TerminatorPlus getInstance() {
        return instance;
    }

    public static String getVersion() {
        return version;
    }

    public BotManagerImpl getManager() {
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
        this.manager = new BotManagerImpl();
        this.handler = new CommandHandler(this);

        TerminatorPlusAPI.setBotManager(manager);
        TerminatorPlusAPI.setInternalBridge(new InternalBridgeImpl());

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
