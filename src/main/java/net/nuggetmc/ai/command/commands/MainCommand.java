package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.OptArg;
import com.jonahseguin.drink.annotation.Sender;
import com.jonahseguin.drink.annotation.Text;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import net.nuggetmc.ai.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MainCommand extends CommandInstance {

    private final PlayerAI plugin;
    private final BotManager manager;
    private final BukkitScheduler scheduler;
    private final DecimalFormat formatter;

    public MainCommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.plugin = PlayerAI.getInstance();
        this.manager = plugin.getManager();
        this.scheduler = Bukkit.getScheduler();
        this.formatter = new DecimalFormat("0.##");
    }

    @Command(
        desc = "The PlayerAI main command."
    )
    public void root(@Sender Player sender) {
        sender.sendMessage(ChatUtils.LINE);
        sender.sendMessage(ChatColor.GOLD + "PlayerAI" + ChatColor.GRAY + " [" + ChatColor.RED + "v" + PlayerAI.getVersion() + ChatColor.GRAY + "]");
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
                 * health
                 * inventory
                 * current target
                 * current kills
                 * skin
                 * neural network values
                 */

                sender.sendMessage(ChatUtils.LINE);
                String botName = bot.getName();
                sender.sendMessage(ChatColor.GREEN + botName);
                //String created = ChatColor.YELLOW + "";
                //sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Created: " + created);
                String world = ChatColor.YELLOW + bot.getBukkitEntity().getWorld().getName();
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "World: " + world);
                Location loc = bot.getLocation();
                String strLoc = ChatColor.YELLOW + formatter.format(loc.getBlockX()) + ", " + formatter.format(loc.getBlockY()) + ", " + formatter.format(loc.getBlockZ());
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Position: " + strLoc);
                Vector vel = bot.getVelocity();
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Velocity: " + vel);
                String strVel = ChatColor.AQUA + formatter.format(vel.getX()) + ", " + formatter.format(vel.getY()) + ", " + formatter.format(vel.getZ());
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
}
