package net.nuggetmc.tplus.command.commands;

import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.IntelligenceAgent;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.command.CommandInstance;
import net.nuggetmc.tplus.command.annotation.Autofill;
import net.nuggetmc.tplus.command.annotation.Command;
import net.nuggetmc.tplus.utils.ChatUtils;
import net.nuggetmc.tplus.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class AICommand extends CommandInstance {

    /*
     * ideas
     * ability to export neural network data to a text file, and also load from them
     * maybe also have a custom extension like .tplus and encrypt it in base64
     */

    private final TerminatorPlus plugin;
    private final BotManager manager;
    private final BukkitScheduler scheduler;

    private IntelligenceAgent agent;

    public AICommand(CommandHandler handler, String name, String description, String... aliases) {
        super(handler, name, description, aliases);

        this.plugin = TerminatorPlus.getInstance();
        this.manager = plugin.getManager();
        this.scheduler = Bukkit.getScheduler();
    }

    @Command
    public void root(CommandSender sender, List<String> args) {
        commandHandler.sendRootInfo(this, sender);
    }

    @Command(
        name = "random",
        desc = "Create bots with random neural networks, collecting feed data.",
        usage = "<amount> <name> [skin]"
    )
    public void random(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            return;
        }

        if (args.size() < 2) {
            commandHandler.sendUsage(sender, this, "random <amount> <name> [skin]");
            return;
        }

        String skin;

        if (args.size() < 3) {
            skin = null;
        } else {
            skin = args.get(2);
        }

        int n;

        try {
            n = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            sender.sendMessage("The amount must be an integer!");
            return;
        }

        manager.createBots((Player) sender, args.get(1), skin, n, NeuralNetwork.RANDOM);
    }

    @Command(
        name = "reinforcement",
        desc = "Begin an AI training session.",
        usage = "<population-size> <name> [skin]"
    )
    public void reinforcement(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            return;
        }

        Player player = (Player) sender;

        if (args.size() < 2) {
            commandHandler.sendUsage(player, this, "reinforcement <amount> <name> [skin]");
            return;
        }

        String skin;

        if (args.size() < 3) {
            skin = null;
        } else {
            skin = args.get(2);
        }

        int populationSize;

        try {
            populationSize = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            player.sendMessage("The population size must be an integer!");
            return;
        }

        if (agent != null) {
            player.sendMessage("A session is already active.");
            return;
        }

        player.sendMessage("Starting a new session...");

        agent = new IntelligenceAgent(this, populationSize, args.get(1), skin);
        agent.addUser(player);
    }

    public IntelligenceAgent getSession() {
        return agent;
    }

    @Command(
        name = "stop",
        desc = "End a currently running AI training session."
    )
    public void stop(CommandSender sender, List<String> args) {
        if (agent == null) {
            sender.sendMessage("No session is currently active.");
            return;
        }

        sender.sendMessage("Stopping the current session...");
        String name = agent.getName();
        clearSession();

        scheduler.runTaskLater(plugin, () -> sender.sendMessage("The session " + ChatColor.YELLOW + name + ChatColor.RESET + " has been closed."), 10);
    }

    public void clearSession() {
        if (agent != null) {
            agent.stop();
            agent = null;
        }
    }

    public boolean hasActiveSession() {
        return agent != null;
    }

    @Command(
        name = "info",
        desc = "Display neural network information about a bot.",
        usage = "<name>",
        autofill = "infoAutofill"
    )
    public void info(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            commandHandler.sendUsage(sender, this, "info <name>");
            return;
        }

        String name = args.get(0);

        sender.sendMessage("Processing request...");

        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Bot bot = manager.getFirst(name);

                if (bot == null) {
                    sender.sendMessage("Could not find bot " + ChatColor.GREEN + name + ChatColor.RESET + "!");
                    return;
                }

                if (!bot.hasNeuralNetwork()) {
                    sender.sendMessage("The bot " + ChatColor.GREEN + name + ChatColor.RESET + " does not have a neural network!");
                    return;
                }

                NeuralNetwork network = bot.getNeuralNetwork();
                List<String> strings = new ArrayList<>();

                network.nodes().forEach((nodeType, node) -> {
                    strings.add("");
                    strings.add(ChatColor.YELLOW + "\"" + nodeType.name().toLowerCase() + "\"" + ChatColor.RESET + ":");
                    List<String> values = new ArrayList<>();
                    node.getValues().forEach((dataType, value) -> values.add(ChatUtils.BULLET_FORMATTED + "node"
                            + dataType.getShorthand().toUpperCase() + ": " + ChatColor.RED + MathUtils.round2Dec(value)));
                    strings.addAll(values);
                });

                sender.sendMessage(ChatUtils.LINE);
                sender.sendMessage(ChatColor.DARK_GREEN + "NeuralNetwork" + ChatUtils.BULLET_FORMATTED + ChatColor.GRAY + "[" + ChatColor.GREEN + name + ChatColor.GRAY + "]");
                strings.forEach(sender::sendMessage);
                sender.sendMessage(ChatUtils.LINE);
            }

            catch (Exception e) {
                sender.sendMessage(ChatUtils.EXCEPTION_MESSAGE);
            }
        });
    }

    @Autofill
    public List<String> infoAutofill(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return manager.fetchNames();
        } else {
            return null;
        }
    }
}
