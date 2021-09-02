package net.nuggetmc.tplus.command;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.tplus.command.annotation.Arg;
import net.nuggetmc.tplus.command.annotation.OptArg;
import net.nuggetmc.tplus.command.annotation.TextArg;
import net.nuggetmc.tplus.command.exception.ArgCountException;
import net.nuggetmc.tplus.command.exception.ArgParseException;
import net.nuggetmc.tplus.command.exception.NonPlayerException;
import net.nuggetmc.tplus.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

        List<Object> parsedArguments = new ArrayList<>();

        int index = 0;

        try {
            for (Parameter parameter : method.getMethod().getParameters()) {
                Class<?> type = parameter.getType();

                boolean required = !parameter.isAnnotationPresent(OptArg.class);

                if (type == CommandSender.class) {
                    parsedArguments.add(sender);
                } else if (type == Player.class) {
                    if (!(sender instanceof Player)) {
                        throw new NonPlayerException();
                    }

                    parsedArguments.add(sender);
                } else if (type == List.class) {
                    parsedArguments.add(arguments);
                }

                else {
                    if (parameter.isAnnotationPresent(TextArg.class)) {
                        if (index >= arguments.size()) {
                            parsedArguments.add("");
                        } else {
                            parsedArguments.add(StringUtils.join(arguments.subList(index, arguments.size()), " "));
                        }

                        continue;
                    }

                    if (index >= arguments.size() && required) {
                        throw new ArgCountException();
                    }

                    String arg;

                    if (index >= arguments.size()) {
                        arg = null;
                    } else {
                        arg = arguments.get(index);
                    }

                    index++;

                    if (type == String.class) {
                        parsedArguments.add(arg);
                    } else if (type == int.class) {
                        if (arg == null) {
                            parsedArguments.add(0);
                            continue;
                        }

                        try {
                            parsedArguments.add(Integer.parseInt(arg));
                        } catch (NumberFormatException e) {
                            throw new ArgParseException(parameter);
                        }
                    } else if (type == double.class) {
                        if (arg == null) {
                            parsedArguments.add(0);
                            continue;
                        }

                        try {
                            parsedArguments.add(Double.parseDouble(arg));
                        } catch (NumberFormatException e) {
                            throw new ArgParseException(parameter);
                        }
                    } else if (type == float.class) {
                        if (arg == null) {
                            parsedArguments.add(0);
                            continue;
                        }

                        try {
                            parsedArguments.add(Float.parseFloat(arg));
                        } catch (NumberFormatException e) {
                            throw new ArgParseException(parameter);
                        }
                    } else if (type == boolean.class) {
                        if (arg == null) {
                            parsedArguments.add(false);
                            continue;
                        }

                        if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")) {
                            parsedArguments.add(Boolean.parseBoolean(arg));
                        } else {
                            throw new ArgParseException(parameter);
                        }
                    } else {
                        parsedArguments.add(arg);
                    }
                }
            }
        }

        catch (NonPlayerException e) {
            sender.sendMessage("This is a player-only command.");
            return true;
        }

        catch (ArgParseException e) {
            Parameter parameter = e.getParameter();
            String name = getArgumentName(parameter);
            sender.sendMessage("The parameter " + ChatColor.YELLOW + name + ChatColor.RESET + " must be of type " + ChatColor.YELLOW + parameter.getType().toString() + ChatColor.RESET + ".");
            return true;
        }

        catch (ArgCountException e) {
            List<String> usageArgs = new ArrayList<>();

            Arrays.stream(method.getMethod().getParameters()).forEach(parameter -> {
                Class<?> type = parameter.getType();

                if (type != CommandSender.class && type != Player.class){
                    usageArgs.add(getArgumentName(parameter));
                }
            });

            sender.sendMessage("Command Usage: " + org.bukkit.ChatColor.YELLOW + "/" + getName() + (method.getName().isEmpty() ? "" : " " + method.getName())
                    + " " + StringUtils.join(usageArgs, " "));
            return true;
        }

        try {
            method.getMethod().invoke(method.getHandler(), parsedArguments.toArray());
        } catch (InvocationTargetException | IllegalAccessException e) {
            sender.sendMessage(ChatColor.RED + "Failed to perform command.");
            e.printStackTrace();
        }

        return true;
    }

    public static String getArgumentName(Parameter parameter) {
        if (parameter.isAnnotationPresent(OptArg.class)) {
            OptArg arg = parameter.getAnnotation(OptArg.class);

            if (!arg.value().isEmpty()) {
                return "[" + ChatUtils.camelToDashed(arg.value()) + "]";
            }
        } else if (parameter.isAnnotationPresent(Arg.class)) {
            Arg arg = parameter.getAnnotation(Arg.class);

            if (!arg.value().isEmpty()) {
                return "<" + ChatUtils.camelToDashed(arg.value()) + ">";
            }
        }

        return "<" + ChatUtils.camelToDashed(parameter.getName()) + ">";
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
