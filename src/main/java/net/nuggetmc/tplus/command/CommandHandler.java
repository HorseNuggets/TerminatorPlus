package net.nuggetmc.tplus.command;

import com.google.common.collect.Sets;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.command.annotation.Command;
import net.nuggetmc.tplus.command.annotation.Require;
import net.nuggetmc.tplus.command.commands.AICommand;
import net.nuggetmc.tplus.command.commands.BotCommand;
import net.nuggetmc.tplus.command.commands.MainCommand;
import net.nuggetmc.tplus.utils.ChatUtils;
import net.nuggetmc.tplus.utils.Debugger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler {

    private final TerminatorPlus plugin;

    private final Map<String, List<String>> help;
    private final Map<String, CommandInstance> commandMap;

    public CommandHandler(TerminatorPlus plugin) {
        this.plugin = plugin;
        this.help = new HashMap<>();
        this.commandMap = new HashMap<>();
        this.registerCommands();
    }

    public Map<String, CommandInstance> getCommands() {
        return commandMap;
    }

    private void registerCommands() {
        registerCommands(
            new MainCommand(this, "terminatorplus", "The TerminatorPlus main command.", "tplus"),
            new BotCommand(this, "bot", "The root command for bot management.", "npc"),
            new AICommand(this, "ai", "The root command for bot AI training.")
        );
    }

    private void registerCommands(CommandInstance... commands) {
        String fallback = plugin.getName().toLowerCase();
        SimpleCommandMap bukkitCommandMap = ((CraftServer) plugin.getServer()).getCommandMap();

        for (CommandInstance command : commands) {
            commandMap.put(command.getName(), command);
            bukkitCommandMap.register(fallback, command);

            Method[] methods = command.getClass().getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    try {
                        method.setAccessible(true);
                    } catch (SecurityException e) {
                        Debugger.log("Failed to access method " + method.getName() + ".");
                        continue;
                    }

                    Command cmd = method.getAnnotation(Command.class);

                    String perm = "";
                    if (method.isAnnotationPresent(Require.class)) {
                        Require require = method.getAnnotation(Require.class);
                        perm = require.value();
                    }

                    String autofillName = cmd.autofill();
                    Method autofiller = null;

                    if (!autofillName.isEmpty()) {
                        for (Method m : methods) {
                            if (m.getName().equals(autofillName)) {
                                autofiller = m;
                            }
                        }
                    }

                    String methodName = cmd.name();
                    CommandMethod commandMethod = new CommandMethod(methodName, Sets.newHashSet(cmd.aliases()), cmd.desc(), perm, command, method, autofiller);

                    command.addMethod(methodName, commandMethod);
                }
            }

            setHelp(command);
        }
    }

    public CommandInstance getCommand(String name) {
        return commandMap.get(name);
    }

    public void sendRootInfo(CommandInstance commandInstance, CommandSender sender) {
        sender.sendMessage(ChatUtils.LINE);
        sender.sendMessage(ChatColor.GOLD + plugin.getName() + ChatUtils.BULLET_FORMATTED + ChatColor.GRAY
                + "[" + ChatColor.YELLOW + "/" + commandInstance.getName() + ChatColor.GRAY + "]");
        help.get(commandInstance.getName()).forEach(sender::sendMessage);
        sender.sendMessage(ChatUtils.LINE);
    }

    private void setHelp(CommandInstance commandInstance) {
        help.put(commandInstance.getName(), getCommandInfo(commandInstance));
    }

    private List<String> getCommandInfo(CommandInstance commandInstance) {
        List<String> output = new ArrayList<>();

        for (CommandMethod method : commandInstance.getMethods().values()) {
            if (!method.getMethod().getAnnotation(Command.class).visible() || method.getName().isEmpty()) {
                continue;
            }

            output.add(ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "/" + commandInstance.getName() + " " + method.getName()
                    + ChatUtils.BULLET_FORMATTED + method.getDescription());
        }

        return output.stream().sorted().collect(Collectors.toList());
    }
}
