package net.nuggetmc.tplus.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.*;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.bot.agent.Agent;
import net.nuggetmc.tplus.bot.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.bot.event.BotDamageByPlayerEvent;
import net.nuggetmc.tplus.bot.event.BotFallDamageEvent;
import net.nuggetmc.tplus.bot.event.BotKilledByPlayerEvent;
import net.nuggetmc.tplus.utils.*;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bot extends EntityPlayer {
    private final DecimalFormat formatter;
    private final TerminatorPlus plugin;
    private final BukkitScheduler scheduler;
    private final Agent agent;

    private NeuralNetwork network;

    public NeuralNetwork getNeuralNetwork() {
        return network;
    }

    public void setNeuralNetwork(NeuralNetwork network) {
        this.network = network;
    }

    public boolean hasNeuralNetwork() {
        return network != null;
    }

    public ItemStack defaultItem;

    private String skinName;

    private boolean shield;
    private boolean blocking;
    private boolean blockUse;

    private Vector velocity;
    private Vector oldVelocity;

    private boolean removeOnDeath;

    private int aliveTicks;
    private int kills;

    private byte fireTicks;
    private byte groundTicks;
    private byte jumpTicks;
    private byte noFallTicks;

    private final Vector offset;

    private Bot(MinecraftServer minecraftServer, WorldServer worldServer, GameProfile profile, PlayerInteractManager manager) {
        super(minecraftServer, worldServer, profile, manager);

        this.formatter = new DecimalFormat("0.##");
        this.plugin = TerminatorPlus.getInstance();
        this.scheduler = Bukkit.getScheduler();
        this.agent = plugin.getManager().getAgent();
        this.defaultItem = new ItemStack(Material.AIR);
        this.velocity = new Vector(0, 0, 0);
        this.oldVelocity = velocity.clone();
        this.noFallTicks = 60;
        this.fireTicks = 0;
        this.removeOnDeath = true;
        this.offset = MathUtils.circleOffset(3);

        datawatcher.set(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte) 0xFF);
    }

    public static Bot createBot(Location loc, String name) {
        return createBot(loc, name, MojangAPI.getSkin(name));
    }

    public static Bot createBot(Location loc, String name, String[] skin) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        UUID uuid = BotUtils.randomSteveUUID();

        CustomGameProfile profile = new CustomGameProfile(uuid, ChatUtils.trim16(name), skin);
        PlayerInteractManager interactManager = new PlayerInteractManager(nmsWorld);

        Bot bot = new Bot(nmsServer, nmsWorld, profile, interactManager);


        bot.playerConnection = new PlayerConnection(nmsServer, new NetworkManager(EnumProtocolDirection.CLIENTBOUND) {

            @Override
            public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) { }

        }, bot);

        bot.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        bot.getBukkitEntity().setNoDamageTicks(0);
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(
        		new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, bot)));

        nmsWorld.addEntity(bot);
        bot.renderAll();
        
        TerminatorPlus.getInstance().getManager().add(bot);

        return bot;
    }

    private void renderAll() {
        Packet<?>[] packets = getRenderPacketsNoInfo();
        Bukkit.getOnlinePlayers().forEach(p -> renderNoInfo(((CraftPlayer) p).getHandle().playerConnection, packets, false));
    }

    private void render(PlayerConnection connection, Packet<?>[] packets, boolean login) {
        connection.sendPacket(packets[0]);
        connection.sendPacket(packets[1]);
        connection.sendPacket(packets[2]);

        if (login) {
            scheduler.runTaskLater(plugin, () -> connection.sendPacket(packets[3]), 10);
        } else {
            connection.sendPacket(packets[3]);
        }
    }
    
    private void renderNoInfo(PlayerConnection connection, Packet<?>[] packets, boolean login) {
        connection.sendPacket(packets[0]);
        connection.sendPacket(packets[1]);

        if (login) {
            scheduler.runTaskLater(plugin, () -> connection.sendPacket(packets[2]), 10);
        } else {
            connection.sendPacket(packets[2]);
        }
    }

    public void render(PlayerConnection connection, boolean login) {
        render(connection, getRenderPackets(), login);
    }

    private Packet<?>[] getRenderPackets() {
        return new Packet[] {
            new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this),
            new PacketPlayOutNamedEntitySpawn(this),
            new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true),
            new PacketPlayOutEntityHeadRotation(this, (byte) ((this.yaw * 256f) / 360f))
        };
    }
    
    private Packet<?>[] getRenderPacketsNoInfo() {
        return new Packet[] {
            new PacketPlayOutNamedEntitySpawn(this),
            new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true),
            new PacketPlayOutEntityHeadRotation(this, (byte) ((this.yaw * 256f) / 360f))
        };
    }

    public void setDefaultItem(ItemStack item) {
        this.defaultItem = item;
    }

    public Vector getOffset() {
        return offset;
    }

    public Vector getVelocity() {
        return velocity.clone();
    }

    public void setVelocity(Vector vector) {
        this.velocity = vector;
    }

    public void addVelocity(Vector vector) { // This can cause lag? (maybe i fixed it with the new static method)
        if (MathUtils.isNotFinite(vector)) {
            velocity = vector;
            return;
        }

        velocity.add(vector);
    }

    public int getAliveTicks() {
        return aliveTicks;
    }

    public boolean tickDelay(int i) {
        return aliveTicks % i == 0;
    }

    private void sendPacket(Packet<?> packet) {
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
    }

    @Override
    public void tick() {
        loadChunks();

        super.tick();

        if (!isAlive()) return;

        aliveTicks++;

        if (fireTicks > 0) --fireTicks;
        if (noDamageTicks > 0) --noDamageTicks;
        if (jumpTicks > 0) --jumpTicks;
        if (noFallTicks > 0) --noFallTicks;

        if (checkGround()) {
            if (groundTicks < 5) groundTicks++;
        } else {
            groundTicks = 0;
        }

        updateLocation();

        float health = getHealth();
        float maxHealth = getMaxHealth();
        float regenAmount = 0.025f;
        float amount;

        if (health < maxHealth - regenAmount) {
            amount = health + regenAmount;
        } else {
            amount = maxHealth;
        }

        setHealth(amount);

        fireDamageCheck();
        fallDamageCheck();
        
        if(locY() < -64) {
            an();
        }

        oldVelocity = velocity.clone();
    }

    private void loadChunks() {
        net.minecraft.server.v1_16_R3.World world = getWorld();

        for (int i = chunkX - 1; i <= chunkX + 1; i++) {
            for (int j = chunkZ - 1; j <= chunkZ + 1; j++) {
                Chunk chunk = world.getChunkAt(i, j);

                if (!chunk.loaded) {
                    chunk.loaded = true;
                }
            }
        }
    }

    private void fireDamageCheck() {
        if (!isAlive()) {
            return; // maybe also have packet reset thing
        }

        Material type = getLocation().getBlock().getType();

        if (type == Material.WATER) {
            setOnFirePackets(false); // maybe also play extinguish noise?
            fireTicks = 0;
            return;
        }

        boolean lava = type == org.bukkit.Material.LAVA;

        if (lava || type == org.bukkit.Material.FIRE || type == Material.SOUL_FIRE) {
            ignite();
        }

        if (noDamageTicks == 0) {
            if (lava) {
                damageEntity(DamageSource.LAVA, 4);
                noDamageTicks = 20;//this used to be 12 ticks but that would cause the bot to take damage too quickly
            } else if (fireTicks > 1) {
                damageEntity(DamageSource.FIRE, 1);
                noDamageTicks = 20;
            }
        }

        if (fireTicks == 1) {
            setOnFirePackets(false);
        }
    }

    public void ignite() {
        if (fireTicks <= 1) setOnFirePackets(true);
        fireTicks = 100;
    }

    public void setOnFirePackets(boolean onFire) {
        datawatcher.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), onFire ? (byte) 1 : (byte) 0);
        sendPacket(new PacketPlayOutEntityMetadata(getId(), datawatcher, false));
    }

    public boolean isOnFire() {
        return fireTicks != 0;
    }

    private void fallDamageCheck() { // TODO create a better bot event system in the future, also have bot.getAgent()
        if (groundTicks != 0 && noFallTicks == 0 && !(oldVelocity.getY() >= -0.8) && !BotUtils.NO_FALL.contains(getLocation().getBlock().getType())) {
            BotFallDamageEvent event = new BotFallDamageEvent(this);

            plugin.getManager().getAgent().onFallDamage(event);

            if (!event.isCancelled()) {
                damageEntity(DamageSource.FALL, (float) Math.pow(3.6, -oldVelocity.getY()));
            }
        }
    }

    public boolean isFalling() {
        return velocity.getY() < -0.8;
    }

    public void block(int blockLength, int cooldown) {
        if (!shield || blockUse) return;
        startBlocking();
        scheduler.runTaskLater(plugin, () -> stopBlocking(cooldown), blockLength);
    }

    private void startBlocking() {
        this.blocking = true;
        this.blockUse = true;
        c(EnumHand.OFF_HAND);
        sendPacket(new PacketPlayOutEntityMetadata(getId(), datawatcher, true));
    }

    private void stopBlocking(int cooldown) {
        this.blocking = false;
        clearActiveItem();
        scheduler.runTaskLater(plugin, () -> this.blockUse = false, cooldown);
        sendPacket(new PacketPlayOutEntityMetadata(getId(), datawatcher, true));
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setShield(boolean enabled) {
        this.shield = enabled;

        setItemOffhand(new org.bukkit.inventory.ItemStack(enabled ? Material.SHIELD : Material.AIR));
    }

    private void updateLocation() {
        double y;

        MathUtils.clean(velocity); // TODO lag????

        if (isInWater()) {
            y = Math.min(velocity.getY() + 0.1, 0.1);
            addFriction(0.8);
            velocity.setY(y);
        }

        else {
            if (groundTicks != 0) {
                velocity.setY(0);
                addFriction(0.5);
                y = 0;
            } else {
                y = velocity.getY();
                velocity.setY(Math.max(y - 0.1, -3.5));
            }
        }

        this.move(EnumMoveType.SELF, new Vec3D(velocity.getX(), y, velocity.getZ()));
    }

    @Override
    public boolean isInWater() {
        Location loc = getLocation();

        for (int i = 0; i <= 2; i++) {
            Material type = loc.getBlock().getType();

            if (type == Material.WATER || type == Material.LAVA) {
                return true;
            }

            loc.add(0, 0.9, 0);
        }

        return false;
    }

    public void jump(Vector vel) {
        if (jumpTicks == 0 && groundTicks > 1) {
            jumpTicks = 4;
            velocity = vel;
        }
    }

    public void jump() {
        jump(new Vector(0, 0.5, 0));
    }

    public void walk(Vector vel) {
        double max = 0.4;

        Vector sum = velocity.clone().add(vel);
        if (sum.length() > max) sum.normalize().multiply(max);

        velocity = sum;
    }

    public void attack(org.bukkit.entity.Entity entity) {
        faceLocation(entity.getLocation());
        punch();

        double damage = ItemUtils.getLegacyAttackDamage(defaultItem);

        if (entity instanceof Damageable) {
            ((Damageable) entity).damage(damage, getBukkitEntity());
        }
    }

    public void punch() {
        swingHand(EnumHand.MAIN_HAND);
    }

    public boolean checkGround() {
        double vy = velocity.getY();

        if (vy > 0) {
            return false;
        }

        World world = getBukkitEntity().getWorld();
        AxisAlignedBB box = getBoundingBox();

        double[] xVals = new double[] {
            box.minX,
            box.maxX
        };

        double[] zVals = new double[] {
            box.minZ,
            box.maxZ
        };

        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(world, x, locY() - 0.01, z);
                Block block = world.getBlockAt(loc);

                if (block.getType().isSolid() && BotUtils.solidAt(loc)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isOnGround() {
        return groundTicks != 0;
    }

    public void addFriction(double factor) {
        double frictionMin = 0.01;

        double x = velocity.getX();
        double z = velocity.getZ();

        velocity.setX(Math.abs(x) < frictionMin ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < frictionMin ? 0 : z * factor);
    }

    public void removeVisually() {
        this.removeTab();
        this.setDead();
    }

    private void removeTab() {
        sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this));
    }

    public void setRemoveOnDeath(boolean enabled) {
        this.removeOnDeath = enabled;
    }

    private void setDead() {
        sendPacket(new PacketPlayOutEntityDestroy(getId()));

        this.dead = true;
        this.defaultContainer.b(this);
        if (this.activeContainer != null) {
            this.activeContainer.b(this);
        }
    }

    private void dieCheck() {
        if (removeOnDeath) {

            // I replaced HashSet with ConcurrentHashMap.newKeySet which creates a "ConcurrentHashSet"
            // this should fix the concurrentmodificationexception mentioned above, I used the ConcurrentHashMap.newKeySet to make a "ConcurrentHashSet"
            plugin.getManager().remove(this);

            scheduler.runTaskLater(plugin, this::setDead, 30);

            this.removeTab();
        }
    }

    @Override
    public void die() {
        super.die();
        this.dieCheck();
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.dieCheck();
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
        net.minecraft.server.v1_16_R3.Entity attacker = damagesource.getEntity();

        float damage;

        boolean playerInstance = attacker instanceof EntityPlayer;

        Player killer;

        if (playerInstance) {
            killer = ((EntityPlayer) attacker).getBukkitEntity();

            BotDamageByPlayerEvent event = new BotDamageByPlayerEvent(this, killer, f);

            agent.onPlayerDamage(event);

            if (event.isCancelled()) {
                return false;
            }

            damage = event.getDamage();
        } else {
            killer = null;
            damage = f;
        }

        boolean damaged = super.damageEntity(damagesource, damage);

        if (!damaged && blocking) {
            getBukkitEntity().getWorld().playSound(getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
        }

        if (damaged && attacker != null) {
            if (playerInstance && !isAlive()) {
                agent.onBotKilledByPlayer(new BotKilledByPlayerEvent(this, killer));
            }

            kb(getLocation(), attacker.getBukkitEntity().getLocation());
        }

        return damaged;
    }

    private void kb(Location loc1, Location loc2) {
        Vector vel = loc1.toVector().subtract(loc2.toVector()).setY(0).normalize().multiply(0.3);

        if (isOnGround()) vel.multiply(0.8).setY(0.4);

        velocity = vel;
    }

    public String getSkinName(){
        return skinName;
    }

    public void setSkinName(String skinName){
        this.skinName = skinName;
    }


    public int getKills() {
        return kills;
    }

    public void incrementKills() {
        kills++;
    }

    public Location getLocation() {
        return getBukkitEntity().getLocation();
    }

    public void faceLocation(Location loc) {
        look(loc.toVector().subtract(getLocation().toVector()), false);
    }

    public void look(BlockFace face) {
        look(face.getDirection(), face == BlockFace.DOWN || face == BlockFace.UP);
    }

    private void look(Vector dir, boolean keepYaw) {
        float yaw, pitch;

        if (keepYaw) {
            yaw = this.yaw;
            pitch = MathUtils.fetchPitch(dir);
        } else {
            float[] vals = MathUtils.fetchYawPitch(dir);
            yaw = vals[0];
            pitch = vals[1];

            sendPacket(new PacketPlayOutEntityHeadRotation(getBukkitEntity().getHandle(), (byte) (yaw * 256 / 360f)));
        }

        setYawPitch(yaw, pitch);
    }

    public void attemptBlockPlace(Location loc, Material type, boolean down) {
        if (down) {
            look(BlockFace.DOWN);
        } else {
            faceLocation(loc);
        }

        setItem(new org.bukkit.inventory.ItemStack(Material.COBBLESTONE));
        punch();

        Block block = loc.getBlock();
        World world = loc.getWorld();

        if (!block.getType().isSolid()) {
            block.setType(type);
            if (world != null) world.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
        }
    }

    public void setItem(org.bukkit.inventory.ItemStack item) {
        setItem(item, EnumItemSlot.MAINHAND);
    }

    public void setItemOffhand(org.bukkit.inventory.ItemStack item) {
        setItem(item, EnumItemSlot.OFFHAND);
    }

    public void setItem(org.bukkit.inventory.ItemStack item, EnumItemSlot slot) {
        if (item == null) item = defaultItem;

        if (slot == EnumItemSlot.MAINHAND) {
            getBukkitEntity().getInventory().setItemInMainHand(item);
        } else if (slot == EnumItemSlot.OFFHAND) {
            getBukkitEntity().getInventory().setItemInOffHand(item);
        }

        sendPacket(new PacketPlayOutEntityEquipment(getId(), new ArrayList<>(Collections.singletonList(
            new Pair<>(slot, CraftItemStack.asNMSCopy(item))
        ))));
    }

    public void swim() {
        getBukkitEntity().setSwimming(true);
        registerPose(EntityPose.SWIMMING);
    }

    public void sneak() {
        getBukkitEntity().setSneaking(true);
        registerPose(EntityPose.CROUCHING);
    }

    public void stand() {
        Player player = getBukkitEntity();
        player.setSneaking(false);
        player.setSwimming(false);

        registerPose(EntityPose.STANDING);
    }

    private void registerPose(EntityPose pose) {
        datawatcher.set(DataWatcherRegistry.s.a(6), pose);
        sendPacket(new PacketPlayOutEntityMetadata(getId(), datawatcher, false));
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


    public List<String> botLore(){

        long aliveTimeHours = TimeUnit.SECONDS.toHours(this.getAliveTicks() / 20);
        long aliveTimeMinutes = TimeUnit.SECONDS.toMinutes((this.getAliveTicks() / 20) % 3600);
        long aliveTimeSeconds = TimeUnit.SECONDS.toSeconds((this.getAliveTicks() / 20) % 60);
        String aliveTime;
        if (aliveTimeHours == 0){
            aliveTime = String.format("%02d", aliveTimeMinutes) + ":" + String.format("%02d", aliveTimeSeconds);
        }
        else{
            aliveTime = aliveTimeHours + ":" + String.format("%02d", aliveTimeMinutes) + ":" + String.format("%02d", aliveTimeSeconds);
        }



        String world = this.getBukkitEntity().getWorld().getName();
        Location loc = this.getLocation();

        String location = org.bukkit.ChatColor.AQUA + formatter.format(loc.getBlockX()) + ", " + formatter.format(loc.getBlockY()) + ", " + formatter.format(loc.getBlockZ());

        Vector vel = this.getVelocity();
        String velocity = org.bukkit.ChatColor.AQUA + formatter.format(vel.getX()) + ", " + formatter.format(vel.getY()) + ", " + formatter.format(vel.getZ());

        String neuralNetwork;
        try{
            neuralNetwork = this.getNeuralNetwork().toString();
        }
        catch (Exception e){
            neuralNetwork = "None";
        }

        List<String> lore = new ArrayList<>();
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "Time Alive - " + net.md_5.bungee.api.ChatColor.YELLOW + aliveTime);
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "World - " + net.md_5.bungee.api.ChatColor.YELLOW + world);
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "Location - " + net.md_5.bungee.api.ChatColor.AQUA + location);
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "Velocity - " + net.md_5.bungee.api.ChatColor.AQUA + velocity);
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "Health - " + net.md_5.bungee.api.ChatColor.RED + this.getHealth());
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "Kills - " + net.md_5.bungee.api.ChatColor.RED + this.getKills());
        lore.add(net.md_5.bungee.api.ChatColor.WHITE + "Neural Network - " + net.md_5.bungee.api.ChatColor.GREEN + neuralNetwork);
        return lore;
    }
}
