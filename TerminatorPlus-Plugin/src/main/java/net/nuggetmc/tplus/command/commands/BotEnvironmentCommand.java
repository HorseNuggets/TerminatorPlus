package net.nuggetmc.tplus.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.nuggetmc.tplus.api.agent.legacyagent.CustomListMode;
import net.nuggetmc.tplus.api.agent.legacyagent.LegacyAgent;
import net.nuggetmc.tplus.api.agent.legacyagent.LegacyMats;
import net.nuggetmc.tplus.api.utils.ChatUtils;
import net.nuggetmc.tplus.command.CommandHandler;
import net.nuggetmc.tplus.command.CommandInstance;
import net.nuggetmc.tplus.command.annotation.Arg;
import net.nuggetmc.tplus.command.annotation.Autofill;
import net.nuggetmc.tplus.command.annotation.Command;

public class BotEnvironmentCommand extends CommandInstance {

    public BotEnvironmentCommand(CommandHandler handler, String name, String description, String... aliases) {
        super(handler, name, description, aliases);
    }

    @Command
    public void root(CommandSender sender, List<String> args) {
        commandHandler.sendRootInfo(this, sender);
    }

    @Command(
            name = "help",
            desc = "Help for /botenvironment.",
            autofill = "autofill"
    )
    public void help(CommandSender sender, List<String> args) {
        if (!args.isEmpty() && args.getFirst().equals("blocks")) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage("If you are running this plugin on a hybrid server, keep in mind that blocks added by mods are not considered solid.");
            sender.sendRichMessage("You must manually add solid blocks added by mods with <yellow>/botenvironment addSolid <material><white>.");
            sender.sendMessage(ChatUtils.LINE);
        } else if (!args.isEmpty() && args.getFirst().equals("mobs")) {
            sender.sendMessage(ChatUtils.LINE);
            sender.sendMessage("The custom mob list is a user-defined list of mobs.");
            sender.sendRichMessage("Use <yellow>/botenvironment addCustomMob <name><white> to add mob types to the list.");
            sender.sendMessage("The list can be used in the CUSTOM_LIST targeting option, and can also be appended to the hostile, raider, or mob targeting options.");
            sender.sendMessage("When appending, the mobs predefined as well as the mobs in the custom list will be considered.");
            sender.sendRichMessage("Use <yellow>/botenvironment mobListType<white> to change the behavior of the custom mob list.");
            sender.sendMessage(ChatUtils.LINE);
        } else {
            sender.sendRichMessage("Do <yellow>/botenvironment help blocks <white>for more information on adding solid blocks.");
            sender.sendRichMessage("Do <yellow>/botenvironment help mobs <white>for more information on creating a custom mob list.");
        }
    }

    @Command(
        name = "getMaterial",
        desc = "Prints out the current material at the specified location.",
        aliases = {"getmat", "getMat", "getmaterial"}
    )
    public void getMaterial(Player sender, @Arg("x") String x, @Arg("y") String y, @Arg("z") String z) {
        Location loc = sender.getLocation().clone();
        try {
            loc.setX(parseDoubleOrRelative(x, loc, 0));
            loc.setY(parseDoubleOrRelative(y, loc, 1));
            loc.setZ(parseDoubleOrRelative(z, loc, 2));
        } catch (NumberFormatException e) {
            sender.sendMessage("A valid location must be provided!");
            return;
        }
        if (!isLocationLoaded(loc)) {
            sender.sendRichMessage(String.format("The location at <blue>(%d, %d, %d)<white> is not loaded.",
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

            return;
        }
        Material mat = loc.getBlock().getType();
        sender.sendRichMessage(String.format("Material at <blue>(%d, %d, %d)<white>: <green>%s<white>",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), mat.name()));

    }

    @Command(
            name = "addSolid",
            desc = "Adds a material to the list of solid materials.",
            aliases = {"addsolid"},
            autofill = "autofill"
    )
    public void addSolid(CommandSender sender, List<String> args) {
        Material mat;
        if (args.size() == 1)
            mat = Material.getMaterial(args.get(0));
        else if (args.size() == 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to specify coordinates!");
                return;
            }
            Location loc = ((Player)sender).getLocation().clone();
            try {
                loc.setX(parseDoubleOrRelative(args.get(0), loc, 0));
                loc.setY(parseDoubleOrRelative(args.get(1), loc, 1));
                loc.setZ(parseDoubleOrRelative(args.get(2), loc, 2));
            } catch (NumberFormatException e) {
                sender.sendRichMessage("A valid location must be provided! <yellow>/bot addSolid <x> <y> <z><white>");
                return;
            }
            if (!isLocationLoaded(loc)) {
                sender.sendRichMessage(String.format("The location at <blue>(%d, %d, %d)<white> is not loaded.",
                        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                return;
            }
            mat = loc.getBlock().getType();
        } else {
            sender.sendMessage("Invalid syntax!");
            sender.sendRichMessage("To specify a material: <yellow>/bot addSolid <material><white>");
            sender.sendRichMessage("To specify a location containing a material: <yellow>/bot addSolid <x> <y> <z><white>");
            return;
        }
        if (mat == null) {
            sender.sendMessage("The material you specified does not exist!");
            return;
        }
        if (LegacyMats.SOLID_MATERIALS.add(mat))
            sender.sendRichMessage("Successfully added <blue>" + mat.name() + "<white> to the list.");
        else
            sender.sendRichMessage("<blue>" + mat.name() + "<white> already exists in the list!");
    }

    @Command(
            name = "removeSolid",
            desc = "Removes a material from the list of solid materials.",
            aliases = {"removesolid"},
            autofill = "autofill"
    )
    public void removeSolid(CommandSender sender, List<String> args) {
        Material mat;
        if (args.size() == 1)
            mat = Material.getMaterial(args.get(0));
        else if (args.size() == 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be a player to specify coordinates!");
                return;
            }
            Location loc = ((Player)sender).getLocation().clone();
            try {
                loc.setX(parseDoubleOrRelative(args.get(0), loc, 0));
                loc.setY(parseDoubleOrRelative(args.get(1), loc, 1));
                loc.setZ(parseDoubleOrRelative(args.get(2), loc, 2));
            } catch (NumberFormatException e) {
                sender.sendRichMessage("A valid location must be provided! <yellow>/bot removeSolid <x> <y> <z><white>");
                return;
            }
            if (!isLocationLoaded(loc)) {
                sender.sendRichMessage(String.format("The location at <blue>(%d, %d, %d)<white> is not loaded.",
                        loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                return;
            }
            mat = loc.getBlock().getType();
        } else {
            sender.sendMessage("Invalid syntax!");
            sender.sendRichMessage("To specify a material: <yellow>/bot removeSolid <material><white>");
            sender.sendRichMessage("To specify a location containing a material: <yellow>/bot removeSolid <x> <y> <z><white>");
            return;
        }
        if (mat == null) {
            sender.sendMessage("The material you specified does not exist!");
            return;
        }
        if (LegacyMats.SOLID_MATERIALS.remove(mat))
            sender.sendRichMessage("Successfully removed <blue>" + mat.name() + "<white> from the list.");
        else
            sender.sendRichMessage("<blue>" + mat.name() + "<white> does not exist in the list!");
    }

    @Command(
            name = "listSolids",
            desc = "Displays the list of solid materials manually added.",
            aliases = {"listsolids"}
    )
    public void listSolids(CommandSender sender) {
        sender.sendMessage(ChatUtils.LINE);
        for (Material mat : LegacyMats.SOLID_MATERIALS)
            sender.sendRichMessage("<green>" + mat.name() + "<white>");
        sender.sendRichMessage("Total items: <blue>" + LegacyMats.SOLID_MATERIALS.size() + "<white>");
        sender.sendMessage(ChatUtils.LINE);
    }

    @Command(
            name = "clearSolids",
            desc = "Clears the list of solid materials manually added.",
            aliases = {"clearsolids"}
    )
    public void clearSolids(CommandSender sender) {
        int size = LegacyMats.SOLID_MATERIALS.size();
        LegacyMats.SOLID_MATERIALS.clear();
        sender.sendRichMessage("Removed all <blue>" + size + "<white> item(s) from the list.");
    }

    @Command(
            name = "addCustomMob",
            desc = "Adds a mob to the custom list.",
            aliases = {"addcustommob"},
            autofill = "autofill"
    )
    public void addCustomMob(CommandSender sender, @Arg("mobName") String mobName) {
        EntityType type = EntityType.fromName(mobName);
        if (type == null) {
            sender.sendMessage("The entity type you specified does not exist!");
            return;
        }
        if (LegacyAgent.CUSTOM_MOB_LIST.add(type))
            sender.sendRichMessage("Successfully added <blue>" + type.name() + "<white> to the list.");
        else
            sender.sendRichMessage("<blue>" + type.name() + "<white> already exists in the list!");
    }

    @Command(
            name = "removeCustomMob",
            desc = "Removes a mob from the custom list.",
            aliases = {"removecustommob"},
            autofill = "autofill"
    )
    public void removeCustomMob(CommandSender sender, @Arg("mobName") String mobName) {
        EntityType type = EntityType.fromName(mobName);
        if (type == null) {
            sender.sendMessage("The entity type you specified does not exist!");
            return;
        }
        if (LegacyAgent.CUSTOM_MOB_LIST.remove(type))
            sender.sendRichMessage("Successfully removed <blue>" + type.name() + "<white> from the list.");
        else
            sender.sendRichMessage("<blue>" + type.name() + "<white> does not exist in the list!");
    }

    @Command(
            name = "listCustomMobs",
            desc = "Displays the custom list of mobs.",
            aliases = {"listcustommobs"}
    )
    public void listCustomMobs(CommandSender sender) {
        sender.sendMessage(ChatUtils.LINE);
        for (EntityType type : LegacyAgent.CUSTOM_MOB_LIST)
            sender.sendRichMessage("<green>" + type.name() + "<white>");
        sender.sendRichMessage("Total items: <blue>" + LegacyAgent.CUSTOM_MOB_LIST.size() + "<white>");
        sender.sendMessage(ChatUtils.LINE);
    }

    @Command(
            name = "clearCustomMobs",
            desc = "Clears the custom list of mobs.",
            aliases = {"clearcustommobs"}
    )
    public void clearCustomMobs(CommandSender sender) {
        int size = LegacyAgent.CUSTOM_MOB_LIST.size();
        LegacyAgent.CUSTOM_MOB_LIST.clear();
        sender.sendRichMessage("Removed all <blue>" + size + "<white> item(s) from the list.");
    }

    @Command(
            name = "mobListType",
            desc = "Changes the behavior of the custom mob list.",
            aliases = {"moblisttype"},
            autofill = "autofill"
    )
    public void mobListType(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendRichMessage("The custom mob list type is <blue>" + LegacyAgent.customListMode + "<white>.");
        } else if (args.size() > 0 && CustomListMode.isValid(args.get(0))) {
            LegacyAgent.customListMode = CustomListMode.from(args.get(0));
            sender.sendRichMessage("Successfully set the custom mob list type to <blue>" + args.get(0) + "<white>.");

        } else
            sender.sendRichMessage("Usage: <yellow>/botenvironment mobListType (" + CustomListMode.listModes() + ")<white>");
    }

    @Autofill
    public List<String> autofill(CommandSender sender, String[] args) {
        List<String> output = new ArrayList<>();
        if (args.length == 2) {
            if (args[0].equals("help")) {
                output.add("blocks");
                output.add("mobs");
            } else if (matches(args[0], "addSolid") || matches(args[0], "removeSolid")) {
                for (Material mat : Material.values())
                    if (!mat.isLegacy())
                        output.add(mat.name());
            } else if (matches(args[0], "addCustomMob") || matches(args[0], "removeCustomMob")) {
                for (EntityType type : EntityType.values())
                    if (type != EntityType.UNKNOWN)
                        output.add(type.name());
            } else if (matches(args[0], "mobListType")) {
                for (CustomListMode mode : CustomListMode.values())
                    output.add(mode.name().toLowerCase(Locale.ENGLISH));
            }
        }
        return output;
    }

    private boolean matches(String input, String check) {
        return input.equals(check) || input.equals(check.toLowerCase(Locale.ENGLISH));
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

    private boolean isLocationLoaded(Location loc) {
        return loc.getWorld().isChunkLoaded(Location.locToBlock(loc.getX()) >> 4, Location.locToBlock(loc.getZ()) >> 4);
    }
}
