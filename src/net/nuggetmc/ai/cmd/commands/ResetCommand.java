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

    private final PlayerAI INSTANCE;

    private final CommandHandler HANDLER;
    private final NPCManager MANAGER;

    private final String NAME;
    private final String DESCRIPTION = "Information about loaded NPCs.";
    private final String CMD_ARGS = "";

    public ResetCommand() {
        this.INSTANCE = PlayerAI.getInstance();
        this.HANDLER = INSTANCE.getHandler();
        this.MANAGER = INSTANCE.getManager();
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
        sender.sendMessage("Removing every NPC...");
        int size = MANAGER.fetch().size();

        MANAGER.reset();

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
