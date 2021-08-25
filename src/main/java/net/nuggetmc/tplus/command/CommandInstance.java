package net.nuggetmc.tplus.command;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandInstance extends BukkitCommand {

    protected final CommandHandler commandHandler;

    private final Map<String, CommandMethod> methods;

    private static final String MANAGE_PERMISSION = "terminatorplus.manage";

    public CommandInstance(CommandHandler handler, String name, String description, @Nullable String... aliases) {
        super(name, description, "", aliases == null ? new ArrayList<>() : Arrays.asList(aliases));

        this.commandHandler = handler;
        this.methods = new HashMap<>();
    }

    public Map<String, CommandMethod> getMethods() {
        return methods;
    }

    protected void addMethod(String name, CommandMethod method) {
        methods.put(name, method);
    }

    @Override
    public boolean execute(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission(MANAGE_PERMISSION)) {
            return false;
        }

        CommandMethod method;

        if (args.length == 0) {
            method = methods.get("");
        } else if (methods.containsKey(args[0])) {
            method = methods.get(args[0]);
        } else {
            method = methods.get("");
        }

        if (method == null) {
            sender.sendMessage(ChatColor.RED + "There is no root command present for the " + ChatColor.YELLOW + getName() + ChatColor.RED + " command.");
            return true;
        }

        List<String> arguments = new ArrayList<>(Arrays.asList(args));

        if (arguments.size() > 0) {
            arguments.remove(0);
        }

        try {
            method.getMethod().invoke(method.getHandler(), sender, arguments);
        } catch (InvocationTargetException | IllegalAccessException e) {
            sender.sendMessage(ChatColor.RED + "Failed to perform command.");
            e.printStackTrace();
        }

        return true;
    }

    @Override
    @Nonnull
    @SuppressWarnings("unchecked")
    public List<String> tabComplete(@Nonnull CommandSender sender, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 1) {
            return methods.keySet().stream().filter(c -> !c.isEmpty()).collect(Collectors.toList());
        }

        if (args.length > 1) {
            CommandMethod commandMethod = methods.get(args[0]);
            Method autofiller = commandMethod.getAutofiller();

            if (autofiller != null) {
                try {
                    return (List<String>) autofiller.invoke(commandMethod.getHandler(), sender, args);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return new ArrayList<>();
    }
}
