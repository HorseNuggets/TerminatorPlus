package net.nuggetmc.ai.command.commands;

import com.jonahseguin.drink.annotation.*;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.BotManager;
import net.nuggetmc.ai.bot.agent.legacyagent.EnumTargetGoal;
import net.nuggetmc.ai.bot.agent.legacyagent.LegacyAgent;
import net.nuggetmc.ai.command.CommandHandler;
import net.nuggetmc.ai.command.CommandInstance;
import net.nuggetmc.ai.utils.Debugger;
import net.nuggetmc.ai.utils.StringUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotCommand extends CommandInstance {

    private final TerminatorPlus plugin;
    private final CommandHandler handler;
    private final BotManager manager;
    private final LegacyAgent agent;
    private final BukkitScheduler scheduler;
    private final DecimalFormat formatter;

    private AICommand aiManager;

    public BotCommand(CommandHandler commandHandler) {
        super(commandHandler);

        this.handler = commandHandler;
        this.plugin = TerminatorPlus.getInstance();
        this.manager = plugin.getManager();
        this.agent = (LegacyAgent) manager.getAgent();
        this.scheduler = Bukkit.getScheduler();
        this.formatter = new DecimalFormat("0.##");
    }

    @Override
    public void onLoad() {
        this.aiManager = (AICommand) handler.getCommand("ai");
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
        name = "give",
        desc = "Gives all bots the specified item",
        usage = "<item>"
    )
    public void give(@Sender Player sender, String item) {
        ItemStack itemToGive = new ItemStack(Material.matchMaterial(item));
        TerminatorPlus.getInstance().getManager().fetch().forEach(b -> b.setDefaultItem(itemToGive));
    }

    @Command(
        name = "info",
        desc = "Information about loaded bots.",
        usage = "[name]",
        autofill = "infoAutofill"
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
                 * neural network values (network name if loaded, otherwise RANDOM)
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
                sender.sendMessage(ChatUtils.EXCEPTION_MESSAGE);
            }
        });
    }

    @Autofill
    public List<String> infoAutofill(CommandSender sender, String[] args) {
        return args.length == 2 ? manager.fetchNames() : null;
    }

    @Command(
        name = "reset",
        desc = "Remove all loaded bots."
    )
    public void reset(@Sender CommandSender sender) {
        sender.sendMessage("Removing every bot...");
        int size = manager.fetch().size();
        manager.reset();
        sender.sendMessage("Removed " + ChatColor.RED + StringUtilities.NUMBER_FORMAT.format(size) + ChatColor.RESET + " entit" + (size == 1 ? "y" : "ies") + ".");

        if (aiManager.hasActiveSession()) {
            Bukkit.dispatchCommand(sender, "ai stop");
        }
    }

    @Command(
        name = "settings",
        desc = "Make changes to the global configuration file and bot-specific settings.",
        aliases = "options",
        autofill = "settingsAutofill"
    )
    public void settings(@Sender CommandSender sender, @OptArg String arg1, @OptArg String arg2) {
        String extra = ChatColor.GRAY + " [" + ChatColor.YELLOW + "/bot settings" + ChatColor.GRAY + "]";

        if (arg1 == null || !arg1.equals("setgoal")) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage(ChatColor.GOLD + "Bot Settings" + extra);
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "setgoal" + ChatUtils.BULLET_FORMATTED + "Set the global bot target selection method.");
            sender.sendMessage(ChatUtils.LINE);
            return;
        }

        EnumTargetGoal goal = EnumTargetGoal.from(arg2 == null ? "" : arg2);

        if (goal == null) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage(ChatColor.GOLD + "Goal Selection Types" + extra);
            Arrays.stream(EnumTargetGoal.values()).forEach(g -> sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + g.name().replace("_", "").toLowerCase()
                    + ChatUtils.BULLET_FORMATTED + g.description()));
            sender.sendMessage(ChatUtils.LINE);
            return;
        }

        agent.setTargetType(goal);

        sender.sendMessage("The global bot goal has been set to " + ChatColor.BLUE + goal.name() + ChatColor.RESET + ".");
    }

    @Autofill
    public List<String> settingsAutofill(CommandSender sender, String[] args) {
        List<String> output = new ArrayList<>();

        // More settings:
        // setitem
        // tpall
        // tprandom
        // hidenametags or nametags <show/hide>
        // sitall
        // lookall

        if (args.length == 2) {
            output.add("setgoal");
        }

        else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("setgoal")) {
                Arrays.stream(EnumTargetGoal.values()).forEach(goal -> output.add(goal.name().replace("_", "").toLowerCase()));
            }
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
