package net.nuggetmc.ai.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandInterface {
    String getName();
    String getDescription();
    String getUsage();

    void onCommand(CommandSender sender, Command cmd, String label, String[] args);
}
