package net.nuggetmc.tplus.api;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.nuggetmc.tplus.api.agent.legacyagent.ai.NeuralNetwork;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface Terminator {

    String getBotName();

    GameProfile getGameProfile();

    NeuralNetwork getNeuralNetwork();

    void setNeuralNetwork(NeuralNetwork neuralNetwork);

    boolean hasNeuralNetwork();

    Location getLocation();

    float getHealth();

    float getMaxHealth();

    void ignite();

    boolean isOnFire();

    boolean isFalling();

    boolean isBlocking();

    void block(int length, int cooldown);

    boolean isInWater();

    void jump(Vector velocity);

    void jump();

    void walk(Vector velocity);

    void look(BlockFace face);

    void attack(Entity target);

    void attemptBlockPlace(Location loc, Material type, boolean down);

    void punch();

    void swim();

    void sneak();

    void stand();

    void addFriction(double factor);

    void removeVisually();

    int getKills();

    void incrementKills();

    void setItem(ItemStack item);

    void setItem(ItemStack item, EquipmentSlot slot);

    void setItemOffhand(ItemStack item);

    void setDefaultItem(ItemStack item);

    Vector getOffset();

    Vector getVelocity();

    void setVelocity(Vector velocity);

    void addVelocity(Vector velocity);

    int getAliveTicks();

    boolean tickDelay(int ticks);
}
