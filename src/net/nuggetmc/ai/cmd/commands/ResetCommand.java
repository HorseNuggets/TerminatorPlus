package net.nuggetmc.ai.cmd.commands;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.cmd.CommandHandler;
import net.nuggetmc.ai.cmd.CommandInterface;
import net.nuggetmc.ai.npc.NPCManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.util.Locale;

public class ResetCommand implements CommandInterface {

    private final PlayerAI plugin;

    private final CommandHandler handler;
    private final NPCManager manager;

    private final String name;
    private final String description = "Information about loaded NPCs.";
    private final String cmdArgs = "";

    public ResetCommand() {
        this.plugin = PlayerAI.getInstance();
        this.handler = plugin.getHandler();
        this.manager = plugin.getManager();
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
        sender.sendMessage("Removing every NPC...");
        int size = manager.fetch().size();

        manager.reset();

        String en;
        if (size == 1) {
            en = "y";
        } else {
            en = "ies";
        }

        String formatted = NumberFormat.getNumberInstance(Locale.US).format(size);
        sender.sendMessage("Removed " + ChatColor.RED + formatted + ChatColor.RESET + " entit" + en + ".");
    }
}
