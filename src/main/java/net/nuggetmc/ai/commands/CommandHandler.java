package net.nuggetmc.ai.commands;

import com.jonahseguin.drink.Drink;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.command.DrinkCommandService;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.commands.commands.PlayerAICommand;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler {

    private final DrinkCommandService drink;
    private final Map<Class<? extends CommandInstance>, List<String>> help;

    public CommandHandler(PlayerAI plugin) {
        drink = (DrinkCommandService) Drink.get(plugin);
        drink.register(new PlayerAICommand(this), "bot", "playerai", "pai", "ai", "npc", "k");
        drink.registerCommands();

        help = new HashMap<>();

        setHelps(new Class[] {
                PlayerAICommand.class
        });
    }

    private void setHelps(Class<? extends CommandInstance>[] cls) {
        for (Class<? extends CommandInstance> clazz : cls) {
            help.put(clazz, getUsage(clazz));
        }
    }

    public List<String> getHelp(Class<? extends CommandInstance> clazz) {
        return help.get(clazz);
    }

    private List<String> getUsage(Class<? extends CommandInstance> clazz) {
        String rootName = getRootName(clazz);

        return getSubCommands(clazz).stream().map(c -> {
            Command command = c.getAnnotation(Command.class);
            return ChatColor.GRAY + " ▪ " + ChatColor.YELLOW + "/" + rootName + " " + command.name() + ChatColor.GRAY + " ▪ "
                    + ChatColor.RESET + command.desc();
        }).sorted().collect(Collectors.toList());
    }

    private String getRootName(Class<? extends CommandInstance> clazz) {
        return drink.getCommands().entrySet().stream()
                .filter(c -> c.getValue().getObject().getClass().isAssignableFrom(clazz)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private List<Method> getSubCommands(Class<? extends CommandInstance> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Command.class) && !m.getAnnotation(Command.class).name().isEmpty()).collect(Collectors.toList());
    }
}
