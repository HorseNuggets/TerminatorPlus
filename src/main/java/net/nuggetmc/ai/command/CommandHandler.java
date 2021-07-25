package net.nuggetmc.ai.command;

import com.jonahseguin.drink.Drink;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.command.DrinkCommandService;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.command.commands.AICommand;
import net.nuggetmc.ai.command.commands.BotCommand;
import net.nuggetmc.ai.command.commands.MainCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler {

    private final TerminatorPlus plugin;

    private static final String MANAGE_PERMISSION = "terminatorplus.manage";

    private final DrinkCommandService drink;
    private final Map<Class<? extends CommandInstance>, List<String>> help;
    private final Map<String, CommandInstance> commandMap;

    public CommandHandler(TerminatorPlus plugin) {
        this.plugin = plugin;
        this.drink = (DrinkCommandService) Drink.get(plugin);
        this.help = new HashMap<>();
        this.commandMap = new HashMap<>();
        this.registerCommands();
        this.drink.registerCommands();
    }

    private void registerCommands() {
        registerCommand(new MainCommand(this, drink), "terminatorplus");
        registerCommand(new BotCommand(this), "bot", "npc");
        registerCommand(new AICommand(this), "ai");
    }

    private void registerCommand(@Nonnull CommandInstance handler, @Nonnull String name, @Nullable String... aliases) {
        handler.setName(name);
        commandMap.put(name, handler);
        drink.register(handler, MANAGE_PERMISSION, name, aliases);
        setHelp(handler.getClass());
    }

    public CommandInstance getComand(String name) {
        return commandMap.get(name);
    }

    public void sendRootInfo(CommandInstance commandInstance, CommandSender sender) {
        sender.sendMessage(ChatUtils.LINE);
        sender.sendMessage(ChatColor.GOLD + plugin.getName() + ChatUtils.BULLET_FORMATTED + ChatColor.GRAY
                + "[" + ChatColor.YELLOW + "/" + commandInstance.getName() + ChatColor.GRAY + "]");
        help.get(commandInstance.getClass()).forEach(sender::sendMessage);
        sender.sendMessage(ChatUtils.LINE);
    }

    private void setHelp(Class<? extends CommandInstance> cls) {
        help.put(cls, getUsage(cls));
    }

    private List<String> getUsage(Class<? extends CommandInstance> cls) {
        String rootName = getRootName(cls);

        return getSubCommands(cls).stream().map(c -> c.getAnnotation(Command.class)).filter(Command::visible).map(c ->
                ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "/" + rootName + " " + c.name() + ChatUtils.BULLET_FORMATTED
                + ChatColor.RESET + c.desc()).sorted().collect(Collectors.toList());
    }

    private String getRootName(Class<? extends CommandInstance> cls) {
        return drink.getCommands().entrySet().stream()
                .filter(c -> c.getValue().getObject().getClass().isAssignableFrom(cls)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private List<Method> getSubCommands(Class<? extends CommandInstance> cls) {
        return Arrays.stream(cls.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Command.class) && !m.getAnnotation(Command.class).name().isEmpty()).collect(Collectors.toList());
    }
}
