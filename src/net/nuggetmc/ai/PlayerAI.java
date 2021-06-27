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

    private final CommandHandler HANDLER;
    private final NPCManager MANAGER;

    public PlayerAI() {
        instance = this;

        this.HANDLER = new CommandHandler();
        this.MANAGER = new NPCManager(this);
    }

    public static PlayerAI getInstance() {
        return instance;
    }

    public CommandHandler getHandler() {
        return HANDLER;
    }

    public NPCManager getManager() {
        return MANAGER;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(MANAGER, this);
        registerCommands();

        MANAGER.connectAll();
    }

    @Override
    public void onDisable() {
        MANAGER.reset();
        MANAGER.disconnectAll();
    }

    private void registerCommands() {
        HANDLER.register(new CommandInterface[] {
            new CreateCommand(),
            new InfoCommand(),
            new DebugCommand(),
            new ResetCommand()
        });

        PluginCommand command = getCommand("playerai");

        if (command != null) {
            command.setExecutor(HANDLER);
            command.setTabCompleter(HANDLER);
        }
    }
}
