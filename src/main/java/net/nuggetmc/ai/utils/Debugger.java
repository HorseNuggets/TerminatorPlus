package net.nuggetmc.ai.utils;

import net.nuggetmc.ai.TerminatorPlus;
import net.nuggetmc.ai.bot.Bot;
import net.nuggetmc.ai.bot.agent.Agent;
import net.nuggetmc.ai.bot.agent.legacyagent.EnumTargetGoal;
import net.nuggetmc.ai.bot.agent.legacyagent.LegacyAgent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.util.Vector;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Debugger {

    private static final String PREFIX = ChatColor.YELLOW + "[DEBUG] " + ChatColor.RESET;

    private final CommandSender sender;

    public Debugger(CommandSender sender) {
        this.sender = sender;
    }

    public static void log(Object... objects) {
        String[] values = formStringArray(objects);
        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(p -> p.sendMessage(PREFIX + String.join(" ", values)));
    }

    private static String[] formStringArray(Object[] objects) {
        return Arrays.stream(objects).map(String::valueOf).toArray(String[]::new);
    }

    private void print(Object... objects) {
        sender.sendMessage(PREFIX + String.join(" ", formStringArray(objects)));
    }

    public void execute(String cmd) {
        try {
            int[] pts = {cmd.indexOf('('), cmd.indexOf(')')};
            if (pts[0] == -1 || pts[1] == -1) throw new IllegalArgumentException();

            String name = cmd.substring(0, pts[0]);
            String content = cmd.substring(pts[0] + 1, pts[1]);

            Object[] args = content.isEmpty() ? null : buildObjects(content);

            Statement statement = new Statement(this, name, args);
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
                String value = str.startsWith(" ") ? str.substring(1) : str;
                Object obj = value;

                try {
                    obj = Double.parseDouble(value);
                } catch (NumberFormatException ignored) { }

                try {
                    obj = Integer.parseInt(value);
                } catch (NumberFormatException ignored) { }

                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    obj = Boolean.parseBoolean(value);
                }

                list.add(obj);
            }
        }

        return list.toArray();
    }

    /*
     * DEBUGGER METHODS
     */

    public void confuse(int n) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        Location loc = player.getLocation();

        double f = n < 100 ? .004 * n : .4;

        for (int i = 0; i < n; i++) {
            Player target = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
            String name = target == null ? "Steve" : target.getName();
            Bot bot = Bot.createBot(loc, name);
            bot.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5).normalize().multiply(f));
            bot.faceLocation(bot.getLocation().add(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5));
        }

        player.getWorld().spawnParticle(Particle.CLOUD, loc, 100, 1, 1, 1, 0.5);
    }

    public void dreamsmp() {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        Location loc = player.getLocation();

        String[] players = new String[] {
            "Dream", "GeorgeNotFound", "Callahan", "Sapnap", "awesamdude", "Ponk", "BadBoyHalo", "TommyInnit", "Tubbo_", "ItsFundy", "Punz",
            "Purpled", "WilburSoot", "Jschlatt", "Skeppy", "The_Eret", "JackManifoldTV", "Nihachu", "Quackity", "KarlJacobs", "HBomb94",
            "Technoblade", "Antfrost", "Ph1LzA", "ConnorEatsPants", "CaptainPuffy", "Vikkstar123", "LazarCodeLazar", "Ranboo", "FoolishG",
            "hannahxxrose", "Slimecicle", "Michaelmcchill"
        };

        double f = .004 * players.length;

        for (String name : players) {
            Bot bot = Bot.createBot(loc, name);
            bot.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5).normalize().multiply(f));
            bot.faceLocation(bot.getLocation().add(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5));
        }

        player.getWorld().spawnParticle(Particle.CLOUD, loc, 100, 1, 1, 1, 0.5);
    }

    public void item() {
        TerminatorPlus.getInstance().getManager().fetch().forEach(b -> b.item = true);
    }

    public void j(boolean b) {
        TerminatorPlus.getInstance().getManager().joinMessages = b;
    }

    public void epic(int n) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        Location loc = player.getLocation();

        double f = n < 100 ? .004 * n : .4;

        for (int i = 0; i < n; i++) {
            String name = PlayerUtils.randomName();
            Bot bot = Bot.createBot(loc, name);
            bot.setVelocity(new Vector(Math.random() - 0.5, 0.5, Math.random() - 0.5).normalize().multiply(f));
            bot.faceLocation(bot.getLocation().add(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5));
        }

        player.getWorld().spawnParticle(Particle.CLOUD, loc, 100, 1, 1, 1, 0.5);
    }

    public void tp() {
        Bot bot = MathUtils.getRandomSetElement(TerminatorPlus.getInstance().getManager().fetch());

        if (bot == null) {
            print("Failed to locate a bot.");
            return;
        }

        print("Located bot", ChatColor.GREEN + bot.getName() + ChatColor.RESET + ".");

        if (sender instanceof Player) {
            print("Teleporting...");
            ((Player) sender).teleport(bot.getLocation());
        }
    }

    public void setTarget(int n) {
        Agent agent = TerminatorPlus.getInstance().getManager().getAgent();
        if (!(agent instanceof LegacyAgent)) {
            print("This method currently only supports " + ChatColor.AQUA + "LegacyAgent" + ChatColor.RESET + ".");
            return;
        }

        LegacyAgent legacyAgent = (LegacyAgent) agent;
        EnumTargetGoal goal = EnumTargetGoal.of(n);

        legacyAgent.setTargetType(goal);

        print("The goal has been set to " + ChatColor.BLUE + goal.name() + ChatColor.RESET + ".");
    }

    public void fire(boolean b) {
        TerminatorPlus.getInstance().getManager().fetch().forEach(bot -> bot.setOnFirePackets(b));
    }

    public void trackYVel() {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(TerminatorPlus.getInstance(), () -> {
            print(player.getVelocity().getY());
        }, 0, 1);
    }

    public void hideNametags() { // this works for some reason
        Set<Bot> bots = TerminatorPlus.getInstance().getManager().fetch();

        for (Bot bot : bots) {
            Location loc = bot.getLocation();
            World world = loc.getWorld();

            if (world == null) continue;

            loc.setX(loc.getBlockX());
            loc.setY(loc.getBlockY());
            loc.setZ(loc.getBlockZ());

            loc.add(0.5, 0.5, 0.5);

            ArmorStand seat = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            seat.setVisible(false);
            seat.setSmall(true);

            bot.getBukkitEntity().setPassenger(seat);
        }
    }

    public void sit() {
        Set<Bot> bots = TerminatorPlus.getInstance().getManager().fetch();

        for (Bot bot : bots) {
            Location loc = bot.getLocation();
            World world = loc.getWorld();

            if (world == null) continue;

            loc.setX(loc.getBlockX());
            loc.setY(loc.getBlockY());
            loc.setZ(loc.getBlockZ());

            loc.add(0.5, -1.5, 0.5);

            ArmorStand seat = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            seat.setVisible(false);
            seat.setGravity(false);
            seat.setSmall(true);

            seat.addPassenger(bot.getBukkitEntity());
        }
    }

    public void look() {
        if (!(sender instanceof Player)) {
            print("Unspecified player.");
            return;
        }

        Player player = (Player) sender;

        for (Bot bot : TerminatorPlus.getInstance().getManager().fetch()) {
            bot.faceLocation(player.getEyeLocation());
        }
    }

    public void toggleAgent() {
        Agent agent = TerminatorPlus.getInstance().getManager().getAgent();

        boolean b = agent.isEnabled();
        agent.setEnabled(!b);

        print("The Bot Agent is now "
                + (b ? ChatColor.RED + "DISABLED" : ChatColor.GREEN + "ENABLED")
                + ChatColor.RESET + ".");
    }
}
