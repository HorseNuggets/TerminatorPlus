package net.nuggetmc.tplus.command.commands;

import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.api.Terminator;
import net.nuggetmc.tplus.api.agent.legacyagent.EnumTargetGoal;
import net.nuggetmc.tplus.api.agent.legacyagent.LegacyAgent;
import net.nuggetmc.tplus.api.utils.ChatUtils;
import net.nuggetmc.tplus.bot.BotManagerImpl;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.command.CommandInstance;
import net.nuggetmc.tplus.command.annotation.*;
import net.nuggetmc.tplus.utils.Debugger;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BotCommand extends CommandInstance {

    private final TerminatorPlus plugin;
    private final CommandHandler handler;
    private final BotManagerImpl manager;
    private final LegacyAgent agent;
    private final BukkitScheduler scheduler;
    private final DecimalFormat formatter;
    private final Map<String, ItemStack[]> armorTiers;
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
    public void root(CommandSender sender) {
        commandHandler.sendRootInfo(this, sender);
    }

    @Command(
            name = "create",
            desc = "Create a bot."
    )
    public void create(CommandSender sender, @Arg("name") String name, @OptArg("skin") String skin, @TextArg @OptArg("loc") String loc) {
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
        manager.createBots(sender, name, skin, 1, location);
    }

    @Command(
            name = "multi",
            desc = "Create multiple bots at once."
    )
    public void multi(CommandSender sender, @Arg("amount") int amount, @Arg("name") String name, @OptArg("skin") String skin, @TextArg @OptArg("loc") String loc) {
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
        manager.createBots(sender, name, skin, amount, location);
    }

    @Command(
            name = "give",
            desc = "Gives a specified item to all bots."
    )
    public void give(CommandSender sender, @Arg("item-name") String itemName) {
        Material type = Material.matchMaterial(itemName);

        if (type == null) {
            sender.sendMessage("The item " + ChatColor.YELLOW + itemName + ChatColor.RESET + " is not valid!");
            return;
        }

        ItemStack item = new ItemStack(type);

        manager.fetch().forEach(bot -> bot.setDefaultItem(item));

        sender.sendMessage("Successfully set the default item to " + ChatColor.YELLOW + item.getType() + ChatColor.RESET + " for all current bots.");
    }

    private void armorTierSetup() {
        armorTiers.put("leather", new ItemStack[]{
                new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_HELMET),
        });

        armorTiers.put("chain", new ItemStack[]{
                new ItemStack(Material.CHAINMAIL_BOOTS),
                new ItemStack(Material.CHAINMAIL_LEGGINGS),
                new ItemStack(Material.CHAINMAIL_CHESTPLATE),
                new ItemStack(Material.CHAINMAIL_HELMET),
        });

        armorTiers.put("gold", new ItemStack[]{
                new ItemStack(Material.GOLDEN_BOOTS),
                new ItemStack(Material.GOLDEN_LEGGINGS),
                new ItemStack(Material.GOLDEN_CHESTPLATE),
                new ItemStack(Material.GOLDEN_HELMET),
        });

        armorTiers.put("iron", new ItemStack[]{
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET),
        });

        armorTiers.put("diamond", new ItemStack[]{
                new ItemStack(Material.DIAMOND_BOOTS),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_HELMET),
        });

        armorTiers.put("netherite", new ItemStack[]{
                new ItemStack(Material.NETHERITE_BOOTS),
                new ItemStack(Material.NETHERITE_LEGGINGS),
                new ItemStack(Material.NETHERITE_CHESTPLATE),
                new ItemStack(Material.NETHERITE_HELMET),
        });
    }

    @Command(
            name = "armor",
            desc = "Gives all bots an armor set.",
            autofill = "armorAutofill"
    )
    public void armor(CommandSender sender, @Arg("armor-tier") String armorTier) {
        String tier = armorTier.toLowerCase();

        if (!armorTiers.containsKey(tier)) {
            sender.sendMessage(ChatColor.YELLOW + tier + ChatColor.RESET + " is not a valid tier!");
            sender.sendMessage("Available tiers: " + ChatColor.YELLOW + String.join(ChatColor.RESET + ", " + ChatColor.YELLOW, armorTiers.keySet()));
            return;
        }

        ItemStack[] armor = armorTiers.get(tier);

        manager.fetch().forEach(bot -> {
            if (bot.getBukkitEntity() instanceof Player botPlayer) {
                botPlayer.getInventory().setArmorContents(armor);
                botPlayer.updateInventory();

                // packet sending to ensure
                bot.setItem(armor[0], EquipmentSlot.FEET);
                bot.setItem(armor[1], EquipmentSlot.LEGS);
                bot.setItem(armor[2], EquipmentSlot.CHEST);
                bot.setItem(armor[3], EquipmentSlot.HEAD);
            }
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
            autofill = "infoAutofill"
    )
    public void info(CommandSender sender, @Arg("bot-name") String name) {
        if (name == null) {
            sender.sendMessage(ChatColor.YELLOW + "Bot GUI coming soon!");
            return;
        }

        sender.sendMessage("Processing request...");

        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Terminator bot = manager.getFirst(name, (sender instanceof Player pl) ? pl.getLocation() : null);

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

                String botName = bot.getBotName();
                String world = ChatColor.YELLOW + bot.getBukkitEntity().getWorld().getName();
                Location loc = bot.getLocation();
                String strLoc = ChatColor.YELLOW + formatter.format(loc.getX()) + ", " + formatter.format(loc.getY()) + ", " + formatter.format(loc.getZ());
                Vector vel = bot.getVelocity();
                String strVel = ChatColor.AQUA + formatter.format(vel.getX()) + ", " + formatter.format(vel.getY()) + ", " + formatter.format(vel.getZ());

                sender.sendMessage(ChatUtils.LINE);
                sender.sendMessage(ChatColor.GREEN + botName);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "World: " + world);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Position: " + strLoc);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + "Velocity: " + strVel);
                sender.sendMessage(ChatUtils.LINE);
            } catch (Exception e) {
                sender.sendMessage(ChatUtils.EXCEPTION_MESSAGE);
            }
        });
    }

    @Autofill
    public List<String> infoAutofill(CommandSender sender, String[] args) {
        return args.length == 2 ? manager.fetchNames() : null;
    }

    @Command(
            name = "count",
            desc = "Counts the amount of bots on screen by name.",
            aliases = {
                    "list"
            }
    )
    public void count(CommandSender sender) {
        List<String> names = manager.fetchNames();
        Map<String, Integer> freqMap = names.stream().collect(Collectors.toMap(s -> s, s -> 1, Integer::sum));
        List<Entry<String, Integer>> entries = freqMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());

        sender.sendMessage(ChatUtils.LINE);
        entries.forEach(en -> sender.sendMessage(ChatColor.GREEN + en.getKey()
                + ChatColor.RESET + " - " + ChatColor.BLUE + en.getValue().toString() + ChatColor.RESET));
        sender.sendMessage("Total bots: " + ChatColor.BLUE + freqMap.values().stream().reduce(0, Integer::sum) + ChatColor.RESET);
        sender.sendMessage(ChatUtils.LINE);
    }

    @Command(
            name = "reset",
            desc = "Remove all loaded bots."
    )
    public void reset(CommandSender sender) {
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

    /*
     * EVENTUALLY, we should make a command parent hierarchy system soon too! (so we don't have to do this crap)
     * basically, in the @Command annotation, you can include a "parent" for the command, so it will be a subcommand under the specified parent
     */
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

        if (arg1 == null || (!arg1.equalsIgnoreCase("setgoal") && !arg1.equalsIgnoreCase("mobtarget") && !arg1.equalsIgnoreCase("playertarget")
                && !arg1.equalsIgnoreCase("addplayerlist") && !arg1.equalsIgnoreCase("region"))) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage(ChatColor.GOLD + "Bot Settings" + extra);
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "setgoal" + ChatUtils.BULLET_FORMATTED + "Set the global bot target selection method.");
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "mobtarget" + ChatUtils.BULLET_FORMATTED + "Allow all bots to be targeted by hostile mobs.");
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "playertarget" + ChatUtils.BULLET_FORMATTED + "Sets a player name for spawned bots to focus on if the goal is PLAYER.");
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "addplayerlist" + ChatUtils.BULLET_FORMATTED + "Adds newly spawned bots to the player list. This allows the bots to be affected by player selectors like @a and @p.");
            sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "region" + ChatUtils.BULLET_FORMATTED + "Sets a region for the bots to prioritize entities inside.");
            sender.sendMessage(ChatUtils.LINE);
            return;
        } else if (arg1.equalsIgnoreCase("setgoal")) {
            if (arg2 == null) {
                sender.sendMessage("The global bot goal is currently " + ChatColor.BLUE + agent.getTargetType() + ChatColor.RESET + ".");
                return;
            }
            EnumTargetGoal goal = EnumTargetGoal.from(arg2);

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
        } else if (arg1.equalsIgnoreCase("mobtarget")) {
            if (arg2 == null) {
                sender.sendMessage("Mob targeting is currently " + (manager.isMobTarget() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.RESET + ".");
                return;
            }
            if (!arg2.equals("true") && !arg2.equals("false")) {
                sender.sendMessage(ChatColor.RED + "You must specify true or false!");
                return;
            }
            manager.setMobTarget(Boolean.parseBoolean(arg2));
            sender.sendMessage("Mob targeting is now " + (manager.isMobTarget() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.RESET + ".");
        } else if (arg1.equalsIgnoreCase("playertarget")) {
            if (args.size() < 2) {
                sender.sendMessage(ChatColor.RED + "You must specify a player name!");
                return;
            }
            String playerName = arg2;
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Could not find player " + ChatColor.YELLOW + playerName + ChatColor.RED + "!");
                return;
            }
            for (Terminator fetch : manager.fetch()) {
                fetch.setTargetPlayer(player.getUniqueId());
            }
            sender.sendMessage("All spawned bots are now set to target " + ChatColor.BLUE + player.getName() + ChatColor.RESET + ". They will target the closest player if they can't be found.\nYou may need to set the goal to PLAYER.");
        } else if (arg1.equalsIgnoreCase("addplayerlist")) {
            if (arg2 == null) {
                sender.sendMessage("Adding bots to the player list is currently " + (manager.addToPlayerList() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.RESET + ".");
                return;
            }
            if (!arg2.equals("true") && !arg2.equals("false")) {
                sender.sendMessage(ChatColor.RED + "You must specify true or false!");
                return;
            }
            manager.setAddToPlayerList(Boolean.parseBoolean(arg2));
            sender.sendMessage("Adding bots to the player list is now " + (manager.addToPlayerList() ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled") + ChatColor.RESET + ".");
        } else if (arg1.equalsIgnoreCase("region")) {
            if (arg2 == null) {
                if (agent.getRegion() == null) {
                    sender.sendMessage("No region has been set.");
                    return;
                }
                sender.sendMessage("The current region is " + ChatColor.BLUE + agent.getRegion() + ChatColor.RESET + ".");
                if (agent.getRegionWeightX() == 0 && agent.getRegionWeightY() == 0 && agent.getRegionWeightZ() == 0)
                    sender.sendMessage("Entities out of range will not be targeted.");
                else {
                    sender.sendMessage("The region X weight is " + ChatColor.BLUE + agent.getRegionWeightX() + ChatColor.RESET + ".");
                    sender.sendMessage("The region Y weight is " + ChatColor.BLUE + agent.getRegionWeightY() + ChatColor.RESET + ".");
                    sender.sendMessage("The region Z weight is " + ChatColor.BLUE + agent.getRegionWeightZ() + ChatColor.RESET + ".");
                }
                return;
            }
            if (arg2.equalsIgnoreCase("clear")) {
                agent.setRegion(null, 0, 0, 0);
                sender.sendMessage("The region has been cleared.");
                return;
            }
            boolean strict = args.size() == 8 && args.get(7).equalsIgnoreCase("strict");
            if (args.size() != 10 && !strict) {
                sender.sendMessage(ChatUtils.LINE);
                sender.sendMessage(ChatColor.GOLD + "Bot Region Settings" + extra);
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "<x1> <y1> <z1> <x2> <y2> <z2> <wX> <wY> <wZ>" + ChatUtils.BULLET_FORMATTED
                        + "Sets a region for bots to prioritize entities within.");
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "<x1> <y1> <z1> <x2> <y2> <z2> strict" + ChatUtils.BULLET_FORMATTED
                        + "Sets a region so that the bots only target entities within the region.");
                sender.sendMessage(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "clear" + ChatUtils.BULLET_FORMATTED
                        + "Clears the region.");
                sender.sendMessage("Without strict mode, the entity distance from the region is multiplied by the weight values if outside the region.");
                sender.sendMessage("The resulting value is added to the entity distance when selecting an entity.");
                sender.sendMessage(ChatUtils.LINE);
                return;
            }
            double x1, y1, z1, x2, y2, z2, wX, wY, wZ;
            try {
                Location loc = sender instanceof Player pl ? pl.getLocation() : null;
                x1 = parseDoubleOrRelative(args.get(1), loc, 0);
                y1 = parseDoubleOrRelative(args.get(2), loc, 1);
                z1 = parseDoubleOrRelative(args.get(3), loc, 2);
                x2 = parseDoubleOrRelative(args.get(4), loc, 0);
                y2 = parseDoubleOrRelative(args.get(5), loc, 1);
                z2 = parseDoubleOrRelative(args.get(6), loc, 2);
                if (strict)
                    wX = wY = wZ = 0;
                else {
                    wX = Double.parseDouble(args.get(7));
                    wY = Double.parseDouble(args.get(8));
                    wZ = Double.parseDouble(args.get(9));
                    if (wX <= 0 || wY <= 0 || wZ <= 0) {
                        sender.sendMessage("The region weights must be positive values!");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("The region bounds and weights must be valid numbers!");
                sender.sendMessage("Correct syntax: " + ChatColor.YELLOW + "/bot settings region <x1> <y1> <z1> <x2> <y2> <z2> <wX> <wY> <wZ>"
                        + ChatColor.RESET);
                return;
            }
            agent.setRegion(new BoundingBox(x1, y1, z1, x2, y2, z2), wX, wY, wZ);
            sender.sendMessage("The region has been set to " + ChatColor.BLUE + agent.getRegion() + ChatColor.RESET + ".");
            if (wX == 0 && wY == 0 && wZ == 0)
                sender.sendMessage("Entities out of range will not be targeted.");
            else {
                sender.sendMessage("The region X weight is " + ChatColor.BLUE + agent.getRegionWeightX() + ChatColor.RESET + ".");
                sender.sendMessage("The region Y weight is " + ChatColor.BLUE + agent.getRegionWeightY() + ChatColor.RESET + ".");
                sender.sendMessage("The region Z weight is " + ChatColor.BLUE + agent.getRegionWeightZ() + ChatColor.RESET + ".");
            }
        }
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
            output.add("mobtarget");
            output.add("playertarget");
            output.add("addplayerlist");
            output.add("region");
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("setgoal")) {
                Arrays.stream(EnumTargetGoal.values()).forEach(goal -> output.add(goal.name().replace("_", "").toLowerCase()));
            }
            if (args[1].equalsIgnoreCase("mobtarget")) {
                output.add("true");
                output.add("false");
            }
            if (args[1].equalsIgnoreCase("playertarget")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    output.add(player.getName());
                }
            }
            if (args[1].equalsIgnoreCase("addplayerlist")) {
                output.add("true");
                output.add("false");
            }
        }

        return output;
    }

    @Command(
            name = "debug",
            desc = "Debug plugin code.",
            visible = false,
            autofill = "debugAutofill"
    )
    public void debug(CommandSender sender, @Arg("expression") String expression) {
        new Debugger(sender).execute(expression);
    }
    
    @Autofill
    public List<String> debugAutofill(CommandSender sender, String[] args) {
    	return args.length == 2 ? new ArrayList<>(Debugger.AUTOFILL_METHODS) : new ArrayList<>();
    }

    private double parseDoubleOrRelative(String pos, Location loc, int type) {
        if (loc == null || pos.length() == 0 || pos.charAt(0) != '~')
            return Double.parseDouble(pos);
        double relative = pos.length() == 1 ? 0 : Double.parseDouble(pos.substring(1));
        switch (type) {
            case 0:
                return relative + Math.round(loc.getX() * 1000) / 1000D;
            case 1:
                return relative + Math.round(loc.getY() * 1000) / 1000D;
            case 2:
                return relative + Math.round(loc.getZ() * 1000) / 1000D;
            default:
                return 0;
        }
    }
}
