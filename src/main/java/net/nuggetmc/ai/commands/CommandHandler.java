package net.nuggetmc.ai.commands;

import com.jonahseguin.drink.CommandService;
import com.jonahseguin.drink.Drink;
import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.command.DrinkCommandContainer;
import com.jonahseguin.drink.command.DrinkCommandService;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.commands.commands.PlayerAICommand;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler {

    private final DrinkCommandService drink;

    public CommandHandler() {
        drink = (DrinkCommandService) Drink.get(PlayerAI.getInstance());
        drink.register(new PlayerAICommand(this), "playerai", "pai");
        drink.registerCommands();
    }

    public List<String> getUsage(Class<? extends CommandInstance> clazz) {
        return getSubCommands(clazz).stream().map(c -> {
            Command command = c.getAnnotation(Command.class);
            return ChatColor.GRAY + " ▪ " + ChatColor.YELLOW + "/" + getRootName(clazz) + (command.name().isEmpty() ? "" : " " + command.name()) + ChatColor.GRAY + " ▪ "
                    + ChatColor.RESET + command.desc();
        }).collect(Collectors.toList());
    }

    private String getRootName(Class<? extends CommandInstance> clazz) {
        return drink.getCommands().entrySet().stream()
                .filter(c -> c.getValue().getObject().getClass().isAssignableFrom(clazz)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private List<Method> getSubCommands(Class<? extends CommandInstance> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(Command.class)).collect(Collectors.toList());
    }

}
