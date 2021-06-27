package net.nuggetmc.ai.cmd.commands;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.cmd.CommandHandler;
import net.nuggetmc.ai.cmd.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InfoCommand implements CommandInterface {

    private final CommandHandler HANDLER;

    private final String NAME;
    private final String DESCRIPTION = "Information about loaded NPCs.";
    private final String CMD_ARGS = "";

    public InfoCommand() {
        this.HANDLER = PlayerAI.getInstance().getHandler();
        this.NAME = HANDLER.fetchName(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getUsage() {
        return CMD_ARGS;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    }
}
