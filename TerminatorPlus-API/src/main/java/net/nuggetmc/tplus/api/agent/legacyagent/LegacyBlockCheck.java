package net.nuggetmc.tplus.api.agent.legacyagent;

import net.nuggetmc.tplus.api.Terminator;
import net.nuggetmc.tplus.api.utils.BotUtils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LegacyBlockCheck {

    private final Plugin plugin;
    private final LegacyAgent agent;

    public LegacyBlockCheck(LegacyAgent agent, Plugin plugin) {
        this.plugin = plugin;
        this.agent = agent;
    }

    private void placeFinal(Terminator bot, LivingEntity player, Location loc) {
        if (loc.getBlock().getType() != Material.COBBLESTONE) {
            for (Player all : Bukkit.getOnlinePlayers())
                all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
            bot.setItem(new ItemStack(Material.COBBLESTONE));
            loc.getBlock().setType(Material.COBBLESTONE);

            Block under = loc.clone().add(0, -1, 0).getBlock();
            if (under.getType() == Material.LAVA) {
                under.setType(Material.COBBLESTONE);
            }
        }
    }

    public void placeBlock(Terminator bot, LivingEntity player, Block block) {

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
                    for (Player all : Bukkit.getOnlinePlayers())
                        all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                    placeFinal(bot, player, b2.getLocation());
                }
            }, 1);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player all : Bukkit.getOnlinePlayers())
                    all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                placeFinal(bot, player, block.getLocation());
            }, 3);
            return;
        }

        for (Player all : Bukkit.getOnlinePlayers())
            all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
        placeFinal(bot, player, block.getLocation());
    }
    
    public boolean tryPreMLG(Terminator bot, Location botLoc) {
    	if(bot.isBotOnGround() || bot.getVelocity().getY() >= -0.8D || bot.getNoFallTicks() > 7)
    		return false;
    	if (tryPreMLG(bot, botLoc, 3))
    		return true;
    	return tryPreMLG(bot, botLoc, 2);
    }
    
    private boolean tryPreMLG(Terminator bot, Location botLoc, int blocksBelow) {
        BoundingBox box = bot.getBotBoundingBox();
        double[] xVals = new double[]{
                box.getMinX(),
                box.getMaxX() - 0.01
        };

        double[] zVals = new double[]{
                box.getMinZ(),
                box.getMaxZ() - 0.01
        };
        Set<Location> below2Set = new HashSet<>();
        
    	for (double x : xVals) {
            for (double z : zVals) {
            	Location below = botLoc.clone();
            	below.setX(x);
            	below.setZ(z);
            	below.setY(bot.getLocation().getBlockY());
            	for (int i = 0; i < blocksBelow - 1; i++) {
            		below.setY(below.getY() - 1);
            		
            		// Blocks before must all be pass-through
            		Material type = below.getBlock().getType();
            		if (LegacyMats.isSolid(type) || LegacyMats.canStandOn(type))
            			return false;
            		below = below.clone();
            	}
            	below.setY(bot.getLocation().getBlockY() - blocksBelow);
            	below2Set.add(below.getBlock().getLocation());
            }
    	}
    	
    	// Second block below must have at least one unplaceable block (that is landable)
    	boolean nether = bot.getDimension() == World.Environment.NETHER;
    	Iterator<Location> itr = below2Set.iterator();
    	while (itr.hasNext()) {
    		Block next = itr.next().getBlock();
    		boolean placeable = nether ? LegacyMats.canPlaceTwistingVines(next)
    			: LegacyMats.canPlaceWater(next, null);
    		if (placeable || (!LegacyMats.isSolid(next.getType()) && !LegacyMats.canStandOn(next.getType())))
    			itr.remove();
    	}
    	
    	// Clutch
    	if (!below2Set.isEmpty()) {
    		List<Location> below2List = new ArrayList<>(below2Set);
    		below2List.sort((a, b) -> {
    			Block aBlock = a.clone().add(0, 1, 0).getBlock();
    			Block bBlock = b.clone().add(0, 1, 0).getBlock();
    			if (aBlock.getType().isAir() && !bBlock.getType().isAir())
    				return -1;
    			if (!bBlock.getType().isAir() && aBlock.getType().isAir())
    				return 1;
    			return Double.compare(BotUtils.getHorizSqDist(a, botLoc), BotUtils.getHorizSqDist(b, botLoc));
    		});

    		Location faceLoc = below2List.getFirst();
    		Location loc = faceLoc.clone().add(0, 1, 0);
            bot.faceLocation(faceLoc);
            bot.look(BlockFace.DOWN);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                bot.faceLocation(faceLoc);
            }, 1);

            bot.punch();
            for (Player all : Bukkit.getOnlinePlayers())
                all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
            bot.setItem(new ItemStack(Material.COBBLESTONE));
            loc.getBlock().setType(Material.COBBLESTONE);
    	}
    	
    	return false;
    }

    public void clutch(Terminator bot, LivingEntity target) {
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
                for (Player all : Bukkit.getOnlinePlayers())
                    all.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
                bot.setItem(new ItemStack(Material.COBBLESTONE));
                loc.getBlock().setType(Material.COBBLESTONE);
            }
        }
    }
}
