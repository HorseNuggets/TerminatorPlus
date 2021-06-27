package net.nuggetmc.ai.cmd.commands;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.cmd.CommandHandler;
import net.nuggetmc.ai.cmd.CommandInterface;
import net.nuggetmc.ai.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand implements CommandInterface {

    private final CommandHandler HANDLER;

    private final String NAME;
    private final String DESCRIPTION = "Create bots.";
    private final String CMD_ARGS = "<name> [skin]";

    public CreateCommand() {
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(HANDLER.nonPlayerMsg());
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(HANDLER.usageMsg(this));
            return;
        }

        String skin;

        if (args.length > 1) {
            skin = args[1];
        } else {
            skin = args[0];
        }

        Player player = (Player) sender;
        NPC npc = NPC.createNPC(args[0], player.getLocation(), skin);
    }
}
