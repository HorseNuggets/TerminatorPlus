package net.nuggetmc.tplus.api;

import com.mojang.authlib.GameProfile;
import net.nuggetmc.tplus.api.agent.legacyagent.ai.NeuralNetwork;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface Terminator {

    String getBotName();

    int getEntityId();

    GameProfile getGameProfile();

    LivingEntity getBukkitEntity();

    NeuralNetwork getNeuralNetwork();

    void setNeuralNetwork(NeuralNetwork neuralNetwork);

    boolean hasNeuralNetwork();

    Location getLocation();

    boolean isBotAlive(); //Has to be named like this because paper re-obfuscates it

    float getBotHealth();

    float getBotMaxHealth();

    void ignite();

    boolean isBotOnFire();

    boolean isFalling();

    boolean isBotBlocking();

    void block(int length, int cooldown);

    boolean isBotInWater();

    boolean isBotOnGround();

    void setBotPitch(float pitch);

    default void setBotXRot(float pitch) {
        setBotPitch(pitch);
    }

    void jump(Vector velocity);

    void jump();

    void walk(Vector velocity);

    void look(BlockFace face);

    void faceLocation(Location location);

    void attack(Entity target);

    void attemptBlockPlace(Location loc, Material type, boolean down);

    void punch();

    void swim();

    void sneak();

    void stand();

    void addFriction(double factor);

    void removeVisually();

    void removeBot();

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

    void renderBot(Object packetListener, boolean login);

    void setOnFirePackets(boolean onFire);
}
