package net.nuggetmc.tplus.command.commands;

import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.Bot;
import net.nuggetmc.tplus.bot.BotManager;
import net.nuggetmc.tplus.bot.agent.legacyagent.EnumTargetGoal;
import net.nuggetmc.tplus.bot.agent.legacyagent.LegacyAgent;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.command.CommandInstance;
import net.nuggetmc.tplus.command.annotation.Autofill;
import net.nuggetmc.tplus.command.annotation.Command;
import net.nuggetmc.tplus.utils.ChatUtils;
import net.nuggetmc.tplus.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;

public class BotCommand extends CommandInstance {

    private final TerminatorPlus plugin;
    private final CommandHandler handler;
    private final BotManager manager;
    private final LegacyAgent agent;
    private final BukkitScheduler scheduler;
    private final DecimalFormat formatter;

    private AICommand aiManager;

    public BotCommand(CommandHandler handler, String name, String description, String... aliases) {
        super(handler, name, description, aliases);

        this.handler = commandHandler;
        this.plugin = TerminatorPlus.getInstance();
        this.manager = plugin.getManager();
        this.agent = (LegacyAgent) manager.getAgent();
        this.scheduler = Bukkit.getScheduler();
        this.formatter = new DecimalFormat("0.##");
        this.armorTiers = new HashMap<>();

        this.armorTierSetup();
    }

    @Command
    public void root(CommandSender sender, List<String> args) {
        commandHandler.sendRootInfo(this, sender);
    }

    @Command(
        name = "create",
        desc = "Create a bot.",
        usage = "<name> [skin]"
    )
    public void create(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            return;
        }

        if (args.isEmpty()) {
            commandHandler.sendUsage(sender, this, "create <name> [skin]");
            return;
        }

        String skin;

        if (args.size() < 2) {
            skin = null;
        } else {
            skin = args.get(1);
        }

        manager.createBots((Player) sender, args.get(0), skin, 1);
    }

    @Command(
        name = "multi",
        desc = "Create multiple bots at once.",
        usage = "<amount> <name> [skin]"
    )
    public void multi(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            return;
        }

        if (args.size() < 2) {
            commandHandler.sendUsage(sender, this, "multi <amount> <name> [skin]");
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

        manager.createBots((Player) sender, args.get(1), skin, n);
    }

    @Command(
        name = "give",
        desc = "Gives a specified item to all bots.",
        usage = "<item>"
    )
    public void give(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            commandHandler.sendUsage(sender, this, "give <item>");
            return;
        }

        String itemName = args.get(0);
        Material type = Material.matchMaterial(itemName);

        if (type == null) {
            sender.sendMessage("The item " + ChatColor.YELLOW + itemName + ChatColor.RESET + " is not valid!");
            return;
        }

        ItemStack item = new ItemStack(type);

        manager.fetch().forEach(bot -> bot.setDefaultItem(item));

        sender.sendMessage("Successfully set the default item to " + ChatColor.YELLOW + item.getType() + ChatColor.RESET + " for all current bots.");
    }

    private final Map<String, ItemStack[]> armorTiers;

    private void armorTierSetup() {
        armorTiers.put("leather", new ItemStack[] {
            new ItemStack(Material.LEATHER_BOOTS),
            new ItemStack(Material.LEATHER_LEGGINGS),
            new ItemStack(Material.LEATHER_CHESTPLATE),
            new ItemStack(Material.LEATHER_HELMET),
        });

        armorTiers.put("chain", new ItemStack[] {
            new ItemStack(Material.CHAINMAIL_BOOTS),
            new ItemStack(Material.CHAINMAIL_LEGGINGS),
            new ItemStack(Material.CHAINMAIL_CHESTPLATE),
            new ItemStack(Material.CHAINMAIL_HELMET),
        });

        armorTiers.put("gold", new ItemStack[] {
            new ItemStack(Material.GOLDEN_BOOTS),
            new ItemStack(Material.GOLDEN_LEGGINGS),
            new ItemStack(Material.GOLDEN_CHESTPLATE),
            new ItemStack(Material.GOLDEN_HELMET),
        });

        armorTiers.put("iron", new ItemStack[] {
            new ItemStack(Material.IRON_BOOTS),
            new ItemStack(Material.IRON_LEGGINGS),
            new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_HELMET),
        });

        armorTiers.put("diamond", new ItemStack[] {
            new ItemStack(Material.DIAMOND_BOOTS),
            new ItemStack(Material.DIAMOND_LEGGINGS),
            new ItemStack(Material.DIAMOND_CHESTPLATE),
            new ItemStack(Material.DIAMOND_HELMET),
        });

        armorTiers.put("netherite", new ItemStack[] {
            new ItemStack(Material.NETHERITE_BOOTS),
            new ItemStack(Material.NETHERITE_LEGGINGS),
            new ItemStack(Material.NETHERITE_CHESTPLATE),
            new ItemStack(Material.NETHERITE_HELMET),
        });
    }

    @Command(
        name = "armor",
        desc = "Gives all bots an armor set.",
        usage = "<armor-tier>",
        autofill = "armorAutofill"
    )
    @SuppressWarnings("deprecation")
    public void armor(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            commandHandler.sendUsage(sender, this, "armor <armor-tier>");
            return;
        }

        String tier = args.get(0).toLowerCase();

        if (!armorTiers.containsKey(tier)) {
            sender.sendMessage(ChatColor.YELLOW + tier + ChatColor.RESET + " is not a valid tier!");
            sender.sendMessage("Available tiers: " + ChatColor.YELLOW + String.join(ChatColor.RESET + ", " + ChatColor.YELLOW, armorTiers.keySet()));
            return;
        }

        ItemStack[] armor = armorTiers.get(tier);

        manager.fetch().forEach(bot -> {
            bot.getBukkitEntity().getInventory().setArmorContents(armor);
            bot.getBukkitEntity().updateInventory();

            // packet sending to ensure
            bot.setItem(armor[0], EnumItemSlot.FEET);
            bot.setItem(armor[1], EnumItemSlot.LEGS);
            bot.setItem(armor[2], EnumItemSlot.CHEST);
            bot.setItem(armor[3], EnumItemSlot.HEAD);
        });

        sender.sendMessage("Successfully set the armor tier to " + ChatColor.YELLOW + tier + ChatColor.RESET + " for all current bots.");
    }

    @Autofill
    public List<String> armorAutofill(CommandSender sender, String[] args) {
        return args.length == 2 ? new ArrayList<>(armorTiers.keySet()) : null;
    }

    @Command(
        name = "info",
        desc = "Information about loaded bots.",
        usage = "[name]",
        autofill = "infoAutofill"
    )
    public void info(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            commandHandler.sendUsage(sender, this, "info <name>");
            return;
        }

        String name = args.get(0);

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
    public void reset(CommandSender sender, List<String> args) {
        sender.sendMessage("Removing every bot...");
        int size = manager.fetch().size();
        manager.reset();
        sender.sendMessage("Removed " + ChatColor.RED + ChatUtils.NUMBER_FORMAT.format(size) + ChatColor.RESET + " entit" + (size == 1 ? "y" : "ies") + ".");

        if (aiManager == null) {
            this.aiManager = (AICommand) handler.getCommand("ai");
        }

        if (aiManager != null && aiManager.hasActiveSession()) {
            Bukkit.dispatchCommand(sender, "ai stop");
        }
    }

    @Command(
        name = "settings",
        desc = "Make changes to the global configuration file and bot-specific settings.",
        aliases = "options",
        autofill = "settingsAutofill"
    )
    public void settings(CommandSender sender, List<String> args) {
        String arg1 = args.isEmpty() ? null : args.get(0);
        String arg2 = args.size() < 2 ? null : args.get(1);

        String extra = ChatColor.GRAY + " [" + ChatColor.YELLOW + "/bot settings" + ChatColor.GRAY + "]";

        if (arg1 == null || (!arg1.equals("setgoal"))) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage(ChatColor.GOLD + "Bot Settings" + extra);
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "setgoal" + ChatUtils.BULLET_FORMATTED + "Set the global bot target selection method.");
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "mobtarget" + ChatUtils.BULLET_FORMATTED + "Allow all future bots spawned to be targetted by hostile mobs.");
            sender.sendMessage(ChatUtils.LINE);
            return;
        }

        EnumTargetGoal goal = EnumTargetGoal.from(arg2 == null ? "" : arg2);

        if (goal == null) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage(ChatColor.GOLD + "Goal Selection Types" + extra);
            Arrays.stream(EnumTargetGoal.values()).forEach(g -> {
                if (g.isTest())
                    return;
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + g.name().replace("_", "").toLowerCase()
                        + ChatUtils.BULLET_FORMATTED + g.description());
            });
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
                Arrays.stream(EnumTargetGoal.values()).forEach(goal -> {
                    if (goal.isTest())
                        return; //don't show test goals
                    output.add(goal.name().replace("_", "").toLowerCase());
                });
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
    public void debug(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            commandHandler.sendUsage(sender, this, "debug <expression>");
            return;
        }

        new Debugger(sender).execute(args.get(0));
    }
}
