package net.nuggetmc.tplus.command.commands;

import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.api.AIManager;
import net.nuggetmc.tplus.api.Terminator;
import net.nuggetmc.tplus.api.agent.legacyagent.ai.IntelligenceAgent;
import net.nuggetmc.tplus.api.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.api.utils.ChatUtils;
import net.nuggetmc.tplus.api.utils.MathUtils;
import net.nuggetmc.tplus.bot.BotManagerImpl;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.command.CommandInstance;
import net.nuggetmc.tplus.command.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class AICommand extends CommandInstance implements AIManager {

    /*
     * ideas
     * ability to export neural network data to a text file, and also load from them
     * maybe also have a custom extension like .tplus and encrypt it in base64
     */

    private final TerminatorPlus plugin;
    private final BotManagerImpl manager;
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
            desc = "Create bots with random neural networks, collecting feed data."
    )
    public void random(CommandSender sender, List<String> args, @Arg("amount") int amount, @Arg("name") String name, @OptArg("skin") String skin, @OptArg("loc") @TextArg String loc) {
        if (sender instanceof Player && args.size() < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ai random <amount> <name> [skin] [spawnLoc: [player Player]/[x,y,z]]");
            return;
        }
        Location location = (sender instanceof Player) ? ((Player) sender).getLocation() : new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        if (loc != null && !loc.isEmpty()) {
            Player player = Bukkit.getPlayer(loc);
            if (player != null) {
                location = player.getLocation();
            } else {
                String[] split = loc.split(" ");
                if (split.length >= 3) {
                    try {
                        double x = Double.parseDouble(split[0]);
                        double y = Double.parseDouble(split[1]);
                        double z = Double.parseDouble(split[2]);
                        World world = Bukkit.getWorld(split.length >= 4 ? split[3] : location.getWorld().getName());
                        location = new Location(world, x, y, z);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("The location '" + ChatColor.YELLOW + loc + ChatColor.RESET + "' is not valid!");
                        return;
                    }
                } else {
                    sender.sendMessage("The location '" + ChatColor.YELLOW + loc + ChatColor.RESET + "' is not valid!");
                    return;
                }
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Spawning bot at 0, 0, 0 in world " + location.getWorld().getName() + " because no location was specified.");
            }
        }
        manager.createBots(sender, name, skin, amount, NeuralNetwork.RANDOM, location);
    }

    @Command(
            name = "reinforcement",
            desc = "Begin an AI training session."
    )
    public void reinforcement(Player sender, @Arg("population-size") int populationSize, @Arg("name") String name, @OptArg("skin") String skin) {
        //FIXME: Sometimes, bots will become invisible, or just stop working if they're the last one alive, this has been partially fixed (invis part) see Terminator#removeBot, which removes the bot.
        //This seems to fix it for the most part, but its still buggy, as the bot will sometimes still freeze
        //see https://cdn.carbonhost.cloud/6201479d7b237373ab269385/screenshots/javaw_DluMN4m0FR.png
        //Blocks are also not placeable where bots have died
        if (agent != null) {
            sender.sendMessage("A session is already active.");
            return;
        }

        sender.sendMessage("Starting a new session...");

        agent = new IntelligenceAgent(this, populationSize, name, skin, plugin, plugin.getManager());
        agent.addUser(sender);
    }

    public IntelligenceAgent getSession() {
        return agent;
    }

    @Command(
            name = "stop",
            desc = "End a currently running AI training session."
    )
    public void stop(CommandSender sender) {
        if (agent == null) {
            sender.sendMessage("No session is currently active.");
            return;
        }

        sender.sendMessage("Stopping the current session...");
        String name = agent.getName();
        clearSession();

        scheduler.runTaskLater(plugin, () -> sender.sendMessage("The session " + ChatColor.YELLOW + name + ChatColor.RESET + " has been closed."), 10);
    }

    @Override
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
            autofill = "infoAutofill"
    )
    public void info(CommandSender sender, @Arg("bot-name") String name) {
        sender.sendMessage("Processing request...");

        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Terminator bot = manager.getFirst(name, (sender instanceof Player pl) ? pl.getLocation() : null);

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
            } catch (Exception e) {
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
