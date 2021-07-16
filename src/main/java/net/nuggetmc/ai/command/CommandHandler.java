package net.nuggetmc.ai.command;

import com.jonahseguin.drink.Drink;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.command.DrinkCommandService;
import com.jonahseguin.drink.utils.ChatUtils;
import net.nuggetmc.ai.command.commands.MainCommand;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler {

    private final DrinkCommandService drink;
    private final Map<Class<? extends CommandInstance>, List<String>> help;

    public CommandHandler(JavaPlugin plugin) {
        drink = (DrinkCommandService) Drink.get(plugin);
        drink.register(new MainCommand(this), "playerai.manage", "bot", "playerai", "pai", "ai", "npc");
        drink.registerCommands();

        help = new HashMap<>();
        setHelps(MainCommand.class);
    }

    @SafeVarargs
    private final void setHelps(Class<? extends CommandInstance>... cls) {
        Arrays.stream(cls).forEach(c -> help.put(c, getUsage(c)));
    }

    public List<String> getHelp(Class<? extends CommandInstance> cls) {
        return help.get(cls);
    }

    private List<String> getUsage(Class<? extends CommandInstance> cls) {
        String rootName = getRootName(cls);

        return getSubCommands(cls).stream().map(c -> {
            Command command = c.getAnnotation(Command.class);
            return ChatUtils.BULLET_FORMATTED + ChatColor.YELLOW + "/" + rootName + " " + command.name() + ChatUtils.BULLET_FORMATTED
                    + ChatColor.RESET + command.desc();
        }).sorted().collect(Collectors.toList());
    }

    private String getRootName(Class<? extends CommandInstance> cls) {
        return drink.getCommands().entrySet().stream()
                .filter(c -> c.getValue().getObject().getClass().isAssignableFrom(cls)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private List<Method> getSubCommands(Class<? extends CommandInstance> cls) {
        return Arrays.stream(cls.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Command.class) && !m.getAnnotation(Command.class).name().isEmpty()).collect(Collectors.toList());
    }
}
