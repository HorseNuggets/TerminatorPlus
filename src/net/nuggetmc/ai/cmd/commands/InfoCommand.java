package net.nuggetmc.ai.cmd.commands;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.cmd.CommandHandler;
import net.nuggetmc.ai.cmd.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InfoCommand implements CommandInterface {

    private final CommandHandler handler;

    private final String name;
    private final String description = "Information about loaded bots.";
    private final String cmdArgs = "";

    public InfoCommand() {
        this.handler = PlayerAI.getInstance().getHandler();
        this.name = handler.fetchName(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUsage() {
        return cmdArgs;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    }
}
