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

    private final CommandHandler handler;

    private final String name;
    private final String description = "Debug NPC stats.";
    private final String cmdArgs = "";

    public DebugCommand() {
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

    private boolean active = false;

    private Set<BukkitRunnable> tasks = new HashSet<>();

    private double round2Dec(double n) {
        return Math.round(n * 100) / 100.0;
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    }
}
