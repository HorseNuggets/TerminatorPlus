package net.nuggetmc.ai.cmd;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.ai.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Map<String, CommandInterface> commands = new HashMap<>();

    private final String prefix = "bot";

    public void register(CommandInterface[] subCmds) {
        for (CommandInterface cmd : subCmds) {
            commands.put(cmd.getName(), cmd);
        }
    }

    public String fetchName(CommandInterface subCmd) {
        String name = subCmd.getClass().getSimpleName();
        return name.substring(0, name.length() - 7).toLowerCase();
    }

    private boolean exists(String name) {
        return commands.containsKey(name.toLowerCase());
    }

    private CommandInterface getExecutor(String name) {
        return commands.get(name.toLowerCase());
    }

    public String nonPlayerMsg() {
        return ChatColor.RED + "You must be a player to execute this command!";
    }

    public String usageMsg(CommandInterface subCmd) {
        return ChatColor.RED + "Invalid arguments!\nUsage: /" + prefix + " " + subCmd.getName() + " " + subCmd.getUsage();
    }

    private void sendCmdInfo(CommandSender sender) {
        sender.sendMessage(ChatUtils.LINE);
        sender.sendMessage(ChatColor.GOLD + "PlayerAI" + ChatColor.GRAY + " [" + ChatColor.RED + "v1.0" + ChatColor.GRAY + "]");

        for (Map.Entry<String, CommandInterface> entry : commands.entrySet()) {
            sender.sendMessage(ChatColor.GRAY + " ▪ " + ChatColor.YELLOW + "/" + prefix + " " + entry.getKey() + ChatColor.GRAY + " ▪ "
                    + ChatColor.RESET +  entry.getValue().getDescription());
        }

        sender.sendMessage(ChatUtils.LINE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || !exists(args[0])) {
            sendCmdInfo(sender);
            return true;
        }

        getExecutor(args[0]).onCommand(sender, cmd, label, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equals("playerai")) return null;

        List<String> groupnames;
        int n = args.length;

        switch (n) {
            case 1:
                groupnames = new ArrayList<>(commands.keySet());
                String arg = args[n - 1];

                if (!isEmptyTab(arg)) {
                    return autofill(groupnames, arg);
                }

                return groupnames;

            default:
                return null;
        }
    }

    private boolean isEmptyTab(String s) {
        return s == null || s.equals("") || s.equals(" ") || s.isEmpty();
    }

    private List<String> autofill(List<String> groupnames, String input) {
        List<String> list = new ArrayList<>();

        for (String entry : groupnames) {
            if (entry.length() >= input.length()) {
                if (input.equalsIgnoreCase(entry.substring(0, input.length()))) {
                    list.add(entry);
                }
            }
        }

        if (list.isEmpty()) {
            return groupnames;
        }

        return list;
    }
}
