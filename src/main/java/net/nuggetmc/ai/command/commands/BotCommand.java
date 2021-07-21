package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.annotation.Text;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import net.nuggetmc.ai.utils.Debugger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BotCommand extends CommandInstance {

    private final TerminatorPlus plugin;
    private final BotManager manager;
    private final BukkitScheduler scheduler;
    private final DecimalFormat formatter;

    public BotCommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.plugin = TerminatorPlus.getInstance();
        this.manager = plugin.getManager();
        this.scheduler = Bukkit.getScheduler();
        this.formatter = new DecimalFormat("0.##");
    }

    @Command(
        desc = "The root command for bot management."
    )
    public void root(@Sender CommandSender sender) {
        commandHandler.sendRootInfo(this, sender);
    }

    @Command(
        name = "create",
        desc = "Create a bot.",
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
        name = "info",
        desc = "Information about loaded bots.",
        usage = "[name]"
    )
    public void info(@Sender CommandSender sender, @OptArg String name) {
        if (name == null) {
            sender.sendMessage(ChatColor.YELLOW + "Bot GUI coming soon!");
            return;
        }

        sender.sendMessage("Processing request...");

        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Bot bot = manager.getFirst(name);

                if (bot == null) {
                    sender.sendMessage("Could not find bot " + ChatColor.GREEN + name + ChatColor.RESET + "!");
                    return;
                }

                /*
                 * time created
                 * current life (how long it has lived for)
                 * health
                 * inventory
                 * current target
                 * current kills
                 * skin
                 * neural network values
                 */

                String botName = bot.getName();
                String world = ChatColor.YELLOW + bot.getBukkitEntity().getWorld().getName();
                Location loc = bot.getLocation();
                String strLoc = ChatColor.YELLOW + formatter.format(loc.getBlockX()) + ", " + formatter.format(loc.getBlockY()) + ", " + formatter.format(loc.getBlockZ());
                Vector vel = bot.getVelocity();
                String strVel = ChatColor.AQUA + formatter.format(vel.getX()) + ", " + formatter.format(vel.getY()) + ", " + formatter.format(vel.getZ());

                sender.sendMessage(ChatUtils.LINE);
                sender.sendMessage(ChatColor.GREEN + botName);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "World: " + world);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Position: " + strLoc);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Velocity: " + strVel);
                sender.sendMessage(ChatUtils.LINE);
            }

            catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "An exception has occured. Please try again.");
            }
        });
    }

    @Command(
        name = "reset",
        desc = "Remove all loaded bots."
    )
    public void reset(@Sender CommandSender sender) {
        sender.sendMessage("Removing every bot...");

        int size = manager.fetch().size();
        manager.reset();

        String formatted = NumberFormat.getNumberInstance(Locale.US).format(size);
        sender.sendMessage("Removed " + ChatColor.RED + formatted + ChatColor.RESET + " entit" + (size == 1 ? "y" : "ies") + ".");
    }

    @Command(
        name = "options",
        desc = "Make changes to the global configuration file and bot-specific settings.",
        aliases = "settings",
        autofill = "optionsAutofill"
    )
    public void options(@Sender CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "This feature is coming soon!");
    }

    public List<String> optionsAutofill(CommandSender sender, String[] args) {
        List<String> output = new ArrayList<>();

        if (args.length == 2) {
            output.add("setgoal");
            output.add("setitem");
            output.add("tpall");
            output.add("tprandom");
            output.add("hidenametags");
            output.add("sitall");
            output.add("lookall");
        }

        return output;
    }

    @Command(
        name = "debug",
        desc = "Debug plugin code.",
        usage = "<expression>",
        visible = false
    )
    public void debug(@Sender CommandSender sender, @Text String cmd) {
        new Debugger(sender).execute(cmd);
    }
}
