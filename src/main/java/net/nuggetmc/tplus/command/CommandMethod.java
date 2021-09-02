package net.nuggetmc.tplus.command;

import java.lang.reflect.Method;
import java.util.Set;

public class CommandMethod {

    private final String name;
    private final Set<String> aliases;
    private final String description;
    private final String permission;

    private final CommandInstance handler;

    private final Method method;
    private final Method autofiller;

    public CommandMethod(String name, Set<String> aliases, String description, String permission, CommandInstance handler, Method method, Method autofiller) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.permission = permission;
        this.handler = handler;
        this.method = method;
        this.autofiller = autofiller;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public CommandInstance getHandler() {
        return handler;
    }

    public Method getMethod() {
        return method;
    }

    public Method getAutofiller() {
        return autofiller;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name=\"" + name + "\",aliases=" + aliases + ",description=\"" + description + "\",permission=\"" + permission + "\",method=" + method + ",autofiller=" + autofiller + "}";
    }
}
