package net.nuggetmc.ai;

import net.nuggetmc.ai.cmd.CommandHandler;
import net.nuggetmc.ai.cmd.CommandInterface;
import net.nuggetmc.ai.cmd.commands.CreateCommand;
import net.nuggetmc.ai.cmd.commands.DebugCommand;
import net.nuggetmc.ai.cmd.commands.InfoCommand;
import net.nuggetmc.ai.cmd.commands.ResetCommand;
import net.nuggetmc.ai.npc.NPCManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerAI extends JavaPlugin {

    private static PlayerAI instance;

    private final CommandHandler handler;
    private final NPCManager manager;

    public PlayerAI() {
        instance = this;

        this.handler = new CommandHandler();
        this.manager = new NPCManager(this);
    }

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
        getServer().getPluginManager().registerEvents(manager, this);
        registerCommands();

        manager.connectAll();
    }

    @Override
    public void onDisable() {
        manager.reset();
        manager.disconnectAll();
    }

    private void registerCommands() {
        handler.register(new CommandInterface[] {
            new CreateCommand(),
            new InfoCommand(),
            new DebugCommand(),
            new ResetCommand()
        });

        PluginCommand command = getCommand("playerai");

        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }
    }
}
