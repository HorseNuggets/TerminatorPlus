package net.nuggetmc.ai.utils;

import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.bot.agent.BotAgent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Debugger {

    private static final String PREFIX = ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET;

    private CommandSender sender;

    public Debugger(CommandSender sender) {
        this.sender = sender;
    }

    private void print(Object... objects) {
        String[] values = Arrays.stream(objects).map(Object::toString).toArray(String[]::new);
        sender.sendMessage(PREFIX + String.join(" ", values));
    }

    public void execute(String cmd) {
        try {
            int[] pts = {cmd.indexOf('('), cmd.indexOf(')')};
            if (pts[0] == -1 || pts[1] == -1) throw new IllegalArgumentException();

            String name = cmd.substring(0, pts[0]);
            String content = cmd.substring(pts[0] + 1, pts[1]);

            Statement statement = new Statement(this, name, new Object[]{content});
            print("Running the expression \"" + ChatColor.AQUA + cmd + ChatColor.RESET + "\"...");
            statement.execute();
        }

        catch (Exception e) {
            print("Error: the expression \"" + ChatColor.AQUA + cmd + ChatColor.RESET + "\" failed to execute.");
            print(e.toString());
        }
    }

    public Object[] buildObjects(String content) {
        List<Object> list = new ArrayList<>();

        if (!content.isEmpty()) {
            String[] values = content.split(",");

            for (String str : values) {
                list.add(str.startsWith(" ") ? str.substring(1) : str);
            }
        }

        return list.toArray();
    }

    public void printObj(String content) {
        if (content.isEmpty()) {
            print("null");
            return;
        }

        Arrays.stream(buildObjects(content)).forEach(this::print);
    }

    public void toggleAgent(String content) {
        BotAgent agent = PlayerAI.getInstance().getManager().getAgent();

        boolean b = agent.isEnabled();
        agent.setEnabled(!b);

        print("The Bot Agent is now "
                + (b ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED")
                + ChatColor.RESET + ".");
    }
}
