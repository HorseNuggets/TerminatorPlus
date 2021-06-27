package net.nuggetmc.ai.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R3.*;
import net.nuggetmc.ai.PlayerAI;
import net.nuggetmc.ai.utils.MojangAPI;
import net.nuggetmc.ai.utils.SteveUUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class NPC extends EntityPlayer {

    public Vector velocity;

    private byte kbTicks;

    private final double regenAmount = 0.05;

    public NPC(MinecraftServer minecraftServer, WorldServer worldServer, GameProfile profile, PlayerInteractManager manager) {
        super(minecraftServer, worldServer, profile, manager);

        velocity = new Vector(0, 0, 0);
        kbTicks = 0;
    }

    public static NPC createNPC(String name, Location loc, String skin) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) loc.getWorld()).getHandle();

        UUID uuid = SteveUUID.generate();

        GameProfile profile = new GameProfile(uuid, name);
        PlayerInteractManager interactManager = new PlayerInteractManager(nmsWorld);

        if (skin != null) {
            setSkin(profile, skin);
        }

        NPC npc = new NPC(nmsServer, nmsWorld, profile, interactManager);

        npc.playerConnection = new PlayerConnection(nmsServer, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), npc);
        npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        npc.getBukkitEntity().setNoDamageTicks(0);
        nmsWorld.addEntity(npc);

        sendSpawnPackets(npc);

        PlayerAI.getInstance().getManager().add(npc);

        return npc;
    }

    private static void setSkin(GameProfile profile, String skin) {
        String[] vals = MojangAPI.getSkin(skin);

        if (vals != null) {
            profile.getProperties().put("textures", new Property("textures", vals[0], vals[1]));
        }
    }

    private static void sendSpawnPackets(NPC npc) {
        DataWatcher watcher = npc.getDataWatcher();
        watcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 0xFF);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            npc.render(connection, false);
        }
    }

    public void render(PlayerConnection connection, boolean login) {
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(this));
        connection.sendPacket(new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true));
        //connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));

        PacketPlayOutEntityHeadRotation rotationPacket = new PacketPlayOutEntityHeadRotation(this, (byte) ((this.yaw * 256f) / 360f));

        if (login) {
            Bukkit.getScheduler().runTaskLater(PlayerAI.getInstance(), () -> connection.sendPacket(rotationPacket), 10);
        } else {
            connection.sendPacket(rotationPacket);
        }
    }

    public void setVelocity(Vector vector) {
        this.velocity = vector;
    }

    public void addVelocity(Vector vector) {
        this.velocity.add(vector);
    }

    @Override
    public void tick() {
        super.tick();

        if (noDamageTicks > 0) --noDamageTicks;
        if (kbTicks > 0) --kbTicks;

        Player playerNPC = this.getBukkitEntity();
        if (playerNPC.isDead()) return;

        double health = playerNPC.getHealth();
        double maxHealth = playerNPC.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
        double amount;

        if (health < maxHealth - regenAmount) {
            amount = health + regenAmount;
        } else {
            amount = maxHealth;
        }

        playerNPC.setHealth(amount);

        updateLocation();
    }

    private void updateLocation() {
        velocity.setY(velocity.getY() - 0.1);

        if (predictGround()) {
            velocity.setY(0);
            addFriction();
        }

        this.move(EnumMoveType.SELF, new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ()));
    }

    private final double bbOffset = 0.05;

    public boolean predictGround() {
        double vy = velocity.getY();

        if (vy > 0) {
            return false;
        }

        double m = vy / 20.0;

        World world = getBukkitEntity().getWorld();
        AxisAlignedBB box = getBoundingBox();

        double[] xVals = new double[] {
            box.minX + bbOffset,
            box.maxX - bbOffset
        };

        double[] zVals = new double[] {
            box.minZ + bbOffset,
            box.maxZ - bbOffset
        };

        for (double x : xVals) {
            for (double z : zVals) {
                double i = locY();

                for (int n = 0; n < 20; n++) {
                    Location test = new Location(world, x, i, z);

                    if (test.getBlock().getType().isSolid()) {
                        return true;
                    }

                    i += m;
                }
            }
        }

        return false;
    }

    public void addFriction() {
        velocity.setX(velocity.getX() * 0.5);
        velocity.setZ(velocity.getZ() * 0.5);
    }

    public void despawn() {
        getBukkitEntity().remove();
    }

    @Override
    public void collide(Entity entity) {
        if (!this.isSameVehicle(entity) && !entity.noclip && !this.noclip) {
            double d0 = entity.locX() - this.locX();
            double d1 = entity.locZ() - this.locZ();
            double d2 = MathHelper.a(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = MathHelper.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;
                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;
                d0 *= 1.0F - this.I;
                d1 *= 1.0F - this.I;

                if (!this.isVehicle()) {
                    velocity.add(new Vector(-d0 * 3, 0.0D, -d1 * 3));
                }

                if (!entity.isVehicle()) {
                    entity.i(d0, 0.0D, d1);
                }
            }
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        boolean damaged = super.damageEntity(damagesource, f);

        net.minecraft.server.v1_16_R3.Entity attacker = damagesource.getEntity();

        if (damaged && kbTicks == 0 && attacker != null) {
            Player player = getBukkitEntity();
            CraftEntity entity = attacker.getBukkitEntity();
            Location loc1 = player.getLocation();
            Location loc2 = entity.getLocation();

            kb(player, loc1, loc2);
        }

        return damaged;
    }

    private void kb(Player playerNPC, Location loc1, Location loc2) {
        Vector diff = loc1.toVector().subtract(loc2.toVector()).normalize();
        diff.multiply(0.25);
        diff.setY(0.5);

        velocity.add(diff);
        kbTicks = 10;
    }

    public void faceLocation(Location loc) {
        try {
            CraftPlayer playerNPC = this.getBukkitEntity();
            Vector dir = loc.toVector().subtract(playerNPC.getLocation().toVector()).normalize();
            Location facing = playerNPC.getLocation().setDirection(dir);
            playerNPC.teleport(facing);

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                connection.sendPacket(new PacketPlayOutEntityHeadRotation(playerNPC.getHandle(), (byte) (facing.getYaw() * 256 / 360)));
            }

        } catch (IllegalArgumentException ignored) { }
    }

    @Override
    public void playerTick() {
        if (this.hurtTicks > 0) {
            this.hurtTicks -= 1;
        }

        entityBaseTick();
        tickPotionEffects();

        this.aU = (int) this.aT;
        this.aL = this.aK;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
    }
}
