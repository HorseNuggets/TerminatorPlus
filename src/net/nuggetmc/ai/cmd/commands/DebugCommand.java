package net.nuggetmc.ai.cmd.commands;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.cmd.CommandHandler;
import net.nuggetmc.ai.cmd.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class DebugCommand implements CommandInterface {

    private final CommandHandler HANDLER;

    private final String NAME;
    private final String DESCRIPTION = "Debug NPC stats.";
    private final String CMD_ARGS = "";

    public DebugCommand() {
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

    private boolean active = false;

    private Set<BukkitRunnable> tasks = new HashSet<>();

    private double round2Dec(double n) {
        return Math.round(n * 100) / 100.0;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    }
}
