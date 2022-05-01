package net.nuggetmc.tplus.bot.agent.legacyagent;

import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.Bot;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LegacyBlockCheck {

    private final TerminatorPlus plugin;
    private final LegacyAgent agent;

    public LegacyBlockCheck(LegacyAgent agent) {
        this.plugin = TerminatorPlus.getInstance();
        this.agent = agent;
    }

    private void placeFinal(Bot bot, Player player, Location loc) {
        if (loc.getBlock().getType() != Material.COBBLESTONE) {
            for (Player all : Bukkit.getOnlinePlayers()) all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
            bot.setItem(new ItemStack(Material.COBBLESTONE));
            loc.getBlock().setType(Material.COBBLESTONE);

            Block under = loc.clone().add(0, -1, 0).getBlock();
            if (under.getType() == Material.LAVA) {
                under.setType(Material.COBBLESTONE);
            }
        }
    }

    public void placeBlock(Bot bot, Player player, Block block) {

        Location loc = block.getLocation();

        Block under = loc.clone().add(0, -1, 0).getBlock();

        if (LegacyMats.SPAWN.contains(under.getType())) {
            placeFinal(bot, player, loc.clone().add(0, -1, 0));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                placeFinal(bot, player, block.getLocation());
            }, 2);
        }

        Set<Block> face = new HashSet<>(Arrays.asList(loc.clone().add(1, 0, 0).getBlock(),
                loc.clone().add(-1, 0, 0).getBlock(),
                loc.clone().add(0, 0, 1).getBlock(),
                loc.clone().add(0, 0, -1).getBlock()));

        boolean a = false;
        for (Block side : face) {
            if (!LegacyMats.SPAWN.contains(side.getType())) {
                a = true;
            }
        }

        if (a) {
            placeFinal(bot, player, block.getLocation());
            return;
        }

        Set<Block> edge = new HashSet<>(Arrays.asList(loc.clone().add(1, -1, 0).getBlock(),
                loc.clone().add(-1, -1, 0).getBlock(),
                loc.clone().add(0, -1, 1).getBlock(),
                loc.clone().add(0, -1, -1).getBlock()));

        boolean b = false;
        for (Block side : edge) {
            if (!LegacyMats.SPAWN.contains(side.getType())) {
                b = true;
            }
        }

        if (b && LegacyMats.SPAWN.contains(under.getType())) {
            placeFinal(bot, player, loc.clone().add(0, -1, 0));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                placeFinal(bot, player, block.getLocation());
            }, 2);
            return;
        }

        Block c1 = loc.clone().add(1, -1, 1).getBlock();
        Block c2 = loc.clone().add(1, -1, -1).getBlock();
        Block c3 = loc.clone().add(-1, -1, 1).getBlock();
        Block c4 = loc.clone().add(-1, -1, -1).getBlock();

        boolean t = false;

        if (!LegacyMats.SPAWN.contains(c1.getType()) || !LegacyMats.SPAWN.contains(c2.getType())) {

            Block b1 = loc.clone().add(1, -1, 0).getBlock();
            if (LegacyMats.SPAWN.contains(b1.getType())) {
                placeFinal(bot, player, b1.getLocation());
            }

            t = true;

        } else if (!LegacyMats.SPAWN.contains(c3.getType()) || !LegacyMats.SPAWN.contains(c4.getType())) {

            Block b1 = loc.clone().add(-1, -1, 0).getBlock();
            if (LegacyMats.SPAWN.contains(b1.getType())) {
                placeFinal(bot, player, b1.getLocation());
            }

            t = true;
        }

        if (t) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block b2 = loc.clone().add(0, -1, 0).getBlock();
                if (LegacyMats.SPAWN.contains(b2.getType())) {
                    for (Player all : Bukkit.getOnlinePlayers()) all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                    placeFinal(bot, player, b2.getLocation());
                }
            }, 1);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player all : Bukkit.getOnlinePlayers()) all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                placeFinal(bot, player, block.getLocation());
            }, 3);
            return;
        }

        for (Player all : Bukkit.getOnlinePlayers()) all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
        placeFinal(bot, player, block.getLocation());
    }

    public void clutch(Bot bot, LivingEntity target) {
        Location botLoc = bot.getLocation();

        Material type = botLoc.clone().add(0, -1, 0).getBlock().getType();
        Material type2 = botLoc.clone().add(0, -2, 0).getBlock().getType();

        if (!(LegacyMats.SPAWN.contains(type) && LegacyMats.SPAWN.contains(type2))) return;

        if (target.getLocation().getBlockY() >= botLoc.getBlockY()) {
            Location loc = botLoc.clone().add(0, -1, 0);

            Set<Block> face = new HashSet<>(Arrays.asList(
                loc.clone().add(1, 0, 0).getBlock(),
                loc.clone().add(-1, 0, 0).getBlock(),
                loc.clone().add(0, 0, 1).getBlock(),
                loc.clone().add(0, 0, -1).getBlock()
            ));

            Location at = null;
            for (Block side : face) {
                if (!LegacyMats.SPAWN.contains(side.getType())) {
                    at = side.getLocation();
                }
            }

            if (at != null) {
                agent.slow.add(bot);
                agent.noFace.add(bot);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    bot.stand();
                    agent.slow.remove(bot);
                }, 12);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    agent.noFace.remove(bot);
                }, 15);

                Location faceLoc = at.clone().add(0, -1.5, 0);

                bot.faceLocation(faceLoc);
                bot.look(BlockFace.DOWN);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    bot.faceLocation(faceLoc);
                }, 1);

                bot.punch();
                bot.sneak();
                for (Player all : Bukkit.getOnlinePlayers()) all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                bot.setItem(new ItemStack(Material.COBBLESTONE));
                loc.getBlock().setType(Material.COBBLESTONE);
            }
        }
    }
}
