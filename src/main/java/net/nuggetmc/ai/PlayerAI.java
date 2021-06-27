package net.nuggetmc.ai;

import net.nuggetmc.ai.commands.CommandHandler;
import net.nuggetmc.ai.npc.NPCManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerAI extends JavaPlugin {

    public static final double VERSION = 1.0;

    private static PlayerAI instance;

    private CommandHandler handler;
    private NPCManager manager;

    public static PlayerAI getInstance() {
        return instance;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    public NPCManager getManager() {
        return manager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Create Instances
        this.handler = new CommandHandler();
        this.manager = new NPCManager(this);

        // Register all the things
        this.registerEvents();

        // Create Netty injections
        manager.connectAll();
    }

    @Override
    public void onDisable() {
        manager.reset();
        manager.disconnectAll();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(manager, this);
    }

}
