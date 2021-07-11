package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.*;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import net.nuggetmc.ai.utils.Debugger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Locale;

public class MainCommand extends CommandInstance {

    private PlayerAI plugin;
    private BotManager manager;

    public MainCommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.plugin = PlayerAI.getInstance();
        this.manager = plugin.getManager();
    }

    @Command(
        desc = "The PlayerAI main command."
    )
    public void root(@Sender Player sender) {
        sender.sendMessage(ChatUtils.LINE);
        sender.sendMessage(ChatColor.GOLD + "PlayerAI" + ChatColor.GRAY + " [" + ChatColor.RED + "v" + PlayerAI.VERSION + ChatColor.GRAY + "]");
        commandHandler.getHelp(getClass()).forEach(sender::sendMessage);
        sender.sendMessage(ChatUtils.LINE);
    }

    @Command(
        name = "create",
        desc = "Create bots.",
        usage = "<name> [skin]"
    )
    public void create(@Sender Player sender, String name, @OptArg String skin) {
        manager.createBots(sender, name, skin, 1);
    }

    @Command(
        name = "multi",
        desc = "Create multiple bots at once.",
        usage = "<amount> <name> [skin]"
    )
    public void multi(@Sender Player sender, int n, String name, @OptArg String skin) {
        manager.createBots(sender, name, skin, n);
    }

    @Command(
        name = "debug",
        desc = "Debug plugin code.",
        usage = "<expression>"
    )
    public void debug(@Sender CommandSender sender, @Text String cmd) {
        new Debugger(sender).execute(cmd);
    }

    @Command(
        name = "info",
        desc = "Information about loaded bots."
    )
    public void info(@Sender Player sender) {
        sender.sendMessage(ChatColor.YELLOW + "Bot GUI coming soon!");
    }

    @Command(
        name = "reset",
        desc = "Remove all loaded bots."
    )
    public void reset(@Sender CommandSender sender) {
        sender.sendMessage("Removing every bot...");

        BotManager manager = PlayerAI.getInstance().getManager();
        int size = manager.fetch().size();
        manager.reset();

        String formatted = NumberFormat.getNumberInstance(Locale.US).format(size);
        sender.sendMessage("Removed " + ChatColor.RED + formatted + ChatColor.RESET + " entit" + (size == 1 ? "y" : "ies") + ".");
    }
}
