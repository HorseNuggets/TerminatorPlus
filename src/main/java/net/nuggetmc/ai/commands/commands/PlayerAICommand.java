package net.nuggetmc.ai.commands.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.annotation.Text;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.commands.CommandHandler;
import net.nuggetmc.ai.commands.CommandInstance;
import net.nuggetmc.ai.npc.NPC;
import net.nuggetmc.ai.npc.NPCManager;
import net.nuggetmc.ai.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;

public class PlayerAICommand extends CommandInstance {

    public PlayerAICommand(CommandHandler commandHandler) {
        super(commandHandler);
    }

    @Command(name = "", desc = "Test Description")
    public void rootCommand(@Sender Player sender) {
        sender.sendMessage(ChatUtils.LINE);
        sender.sendMessage(ChatColor.GOLD + "PlayerAI" + ChatColor.GRAY + " [" + ChatColor.RED + "v" + PlayerAI.VERSION + ChatColor.GRAY + "]");

        for (String s : this.getCommandHandler().getUsage(PlayerAICommand.class)) {
            sender.sendMessage(s);
        }

        sender.sendMessage(ChatUtils.LINE);
    }

    @Command(name = "create", desc = "Create bots.", usage = "<name> [skin]")
    public void createBotCommand(@Sender Player sender, String name, @OptArg("Technoblade") String skin) {
        NPC.createNPC(name, sender.getLocation(), skin.isEmpty() ? name : skin);
    }

    @Command(name = "debug", desc = "Debug bot stats.")
    public void debugCommand(@Sender Player sender) {

    }

    @Command(name = "info", desc = "Information about loaded bots.")
    public void infoCommand(@Sender Player sender) {

    }

    @Command(name = "reset", desc = "Remove all loaded bots.")
    public void resetCommand(@Sender Player sender) {
        sender.sendMessage("Removing every bot...");
        NPCManager manager = PlayerAI.getInstance().getManager();
        int size = manager.fetch().size();
        manager.reset();
        String formatted = NumberFormat.getNumberInstance(Locale.US).format(size);
        sender.sendMessage("Removed " + ChatColor.RED + formatted + ChatColor.RESET + " entit" + (size == 1 ? "y" : "ies") + ".");
    }

}
