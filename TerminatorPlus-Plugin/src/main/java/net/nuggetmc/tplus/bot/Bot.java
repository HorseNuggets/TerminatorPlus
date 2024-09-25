package net.nuggetmc.tplus.bot;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nuggetmc.tplus.TerminatorPlus;
import net.nuggetmc.tplus.api.Terminator;
import net.nuggetmc.tplus.api.agent.Agent;
import net.nuggetmc.tplus.api.agent.legacyagent.LegacyMats;
import net.nuggetmc.tplus.api.agent.legacyagent.ai.NeuralNetwork;
import net.nuggetmc.tplus.api.event.BotDamageByPlayerEvent;
import net.nuggetmc.tplus.api.event.BotFallDamageEvent;
import net.nuggetmc.tplus.api.event.BotKilledByPlayerEvent;
import net.nuggetmc.tplus.api.utils.*;
import net.nuggetmc.tplus.nms.MockConnection;
import net.nuggetmc.tplus.utils.NMSUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_20_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;

public class Bot extends ServerPlayer implements Terminator {

    private final TerminatorPlus plugin;
    private final BukkitScheduler scheduler;
    private final Agent agent;
    private final Vector offset;
    public ItemStack defaultItem;
    private NeuralNetwork network;
    private boolean shield;
    private boolean blocking;
    private boolean blockUse;
    private Vector velocity;
    private Vector oldVelocity;
    private boolean removeOnDeath;
    private int aliveTicks;
    private int kills;
    private byte groundTicks;
    private byte jumpTicks;
    private byte noFallTicks;
    private List<Block> standingOn = new ArrayList<>();
    private UUID targetPlayer = null;
    private boolean inPlayerList;

    private Bot(MinecraftServer minecraftServer, ServerLevel worldServer, GameProfile profile, boolean addToPlayerList) {
        super(minecraftServer, worldServer, profile, ClientInformation.createDefault());

        this.plugin = TerminatorPlus.getInstance();
        this.scheduler = Bukkit.getScheduler();
        this.agent = plugin.getManager().getAgent();
        this.defaultItem = new ItemStack(Material.AIR);
        this.velocity = new Vector(0, 0, 0);
        this.oldVelocity = velocity.clone();
        this.noFallTicks = 60;
        this.removeOnDeath = true;
        this.offset = MathUtils.circleOffset(3);
        if (addToPlayerList) {
            minecraftServer.getPlayerList().getPlayers().add(this);
            inPlayerList = true;
        }

        //this.entityData.set(new EntityDataAccessor<>(16, EntityDataSerializers.BYTE), (byte) 0xFF);
    }

    public static Bot createBot(Location loc, String name) {
        return createBot(loc, name, MojangAPI.getSkin(name));
    }

    public static Bot createBot(Location loc, String name, String[] skin) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        UUID uuid = BotUtils.randomSteveUUID();

        CustomGameProfile profile = new CustomGameProfile(uuid, ChatUtils.trim16(name), skin);

        boolean addPlayerList = TerminatorPlus.getInstance().getManager().addToPlayerList();

        Bot bot = new Bot(nmsServer, nmsWorld, profile, addPlayerList);

        bot.connection = new ServerGamePacketListenerImpl(nmsServer, new MockConnection(), bot, CommonListenerCookie.createInitial(bot.getGameProfile()));

        bot.setPos(loc.getX(), loc.getY(), loc.getZ());
        bot.setRot(loc.getYaw(), loc.getPitch());
        bot.getBukkitEntity().setNoDamageTicks(0);
        if (addPlayerList) {
            Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(
                    ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(bot))));
            nmsWorld.addNewPlayer(bot);
        } else {
            Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(
                    new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, bot)));
            nmsWorld.addFreshEntity(bot);
        }
        bot.renderAll();

        TerminatorPlus.getInstance().getManager().add(bot);

        return bot;
    }

    @Override
    public String getBotName() {
        return displayName;
    }

    @Override
    public int getEntityId() {
        return getId();
    }

    @Override
    public NeuralNetwork getNeuralNetwork() {
        return network;
    }

    @Override
    public void setNeuralNetwork(NeuralNetwork network) {
        this.network = network;
    }

    @Override
    public boolean hasNeuralNetwork() {
        return network != null;
    }

    private void renderAll() {
        Packet<?>[] packets = getRenderPacketsNoInfo();
        this.entityData.set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 0x7F);
        Bukkit.getOnlinePlayers().forEach(p -> renderNoInfo(((CraftPlayer) p).getHandle().connection, packets, false));
    }

    private void render(ServerGamePacketListenerImpl connection, Packet<?>[] packets, boolean login) {
        connection.send(packets[0]);
        connection.send(packets[1]);
        connection.send(packets[2]);

        if (login) {
            scheduler.runTaskLater(plugin, () -> connection.send(packets[3]), 10);
        } else {
            connection.send(packets[3]);
        }
    }

    private void renderNoInfo(ServerGamePacketListenerImpl connection, Packet<?>[] packets, boolean login) {
        connection.send(packets[0]);
        connection.send(packets[1]);

        if (login) {
            scheduler.runTaskLater(plugin, () -> connection.send(packets[2]), 10);
        } else {
            connection.send(packets[2]);
        }
    }

    public void render(ServerGamePacketListenerImpl connection, boolean login) {
        render(connection, getRenderPackets(), login);
    }

    @Override
    public void renderBot(Object packetListener, boolean login) {
        if (!(packetListener instanceof ServerGamePacketListenerImpl)) {
            throw new IllegalArgumentException("packetListener must be a instance of ServerGamePacketListenerImpl");
        }
        render((ServerGamePacketListenerImpl) packetListener, login);
    }

    private Packet<?>[] getRenderPackets() {
        return new Packet[]{
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this),
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this),
                //new ClientboundSetEntityDataPacket(this.getId(), this.entityData, true),
                new ClientboundSetEntityDataPacket(this.getId(), NMSUtils.getEntityData(this.entityData)),
                new ClientboundRotateHeadPacket(this, (byte) ((this.yHeadRot * 256f) / 360f))
        };
    }

    private Packet<?>[] getRenderPacketsNoInfo() {
        return new Packet[]{
                new ClientboundAddEntityPacket(this),
                //new ClientboundSetEntityDataPacket(this.getId(), this.entityData, true),
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this),
                new ClientboundSetEntityDataPacket(this.getId(), this.entityData.packDirty()),
                new ClientboundRotateHeadPacket(this, (byte) ((this.yHeadRot * 256f) / 360f))
        };
    }

    @Override
    public void setDefaultItem(ItemStack item) {
        this.defaultItem = item;
    }

    @Override
    public Vector getOffset() {
        return offset;
    }

    @Override
    public Vector getVelocity() {
        return velocity.clone();
    }

    @Override
    public void setVelocity(Vector vector) {
        this.velocity = vector;
    }

    @Override
    public void addVelocity(Vector vector) { // This can cause lag? (maybe i fixed it with the new static method)
        if (MathUtils.isNotFinite(vector)) {
            velocity = vector;
            return;
        }

        velocity.add(vector);
    }

    @Override
    public int getAliveTicks() {
        return aliveTicks;
    }

    @Override
    public int getNoFallTicks() {
        return noFallTicks;
    }

    @Override
    public boolean tickDelay(int i) {
        return aliveTicks % i == 0;
    }

    private void sendPacket(Packet<?> packet) {
        Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer) p).getHandle().connection.send(packet));
    }

    @Override
    public boolean isBotAlive() {
        return isAlive();
    }

    @Override
    public float getBotHealth() {
        return getHealth();
    }

    @Override
    public float getBotMaxHealth() {
        return getMaxHealth();
    }

    @Override
    public void tick() {
        loadChunks();

        super.tick();

        if (!isAlive()) return;

        aliveTicks++;

        if (jumpTicks > 0) --jumpTicks;
        if (noFallTicks > 0) --noFallTicks;

        if (checkGround()) {
            if (groundTicks < 5) groundTicks++;
        } else {
            groundTicks = 0;
        }

        updateLocation();

        if (!isAlive()) return;

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

        fallDamageCheck();

        oldVelocity = velocity.clone();

        doTick();
    }

    private void loadChunks() {
        Level world = level();

        for (int i = chunkPosition().x - 1; i <= chunkPosition().x + 1; i++) {
            for (int j = chunkPosition().z - 1; j <= chunkPosition().z + 1; j++) {
                LevelChunk chunk = world.getChunk(i, j);

                if (!chunk.loaded) {
                    chunk.loaded = true;
                }
            }
        }
    }

    @Override
    public UUID getTargetPlayer() {
        return targetPlayer;
    }

    @Override
    public void setTargetPlayer(UUID target) {
        this.targetPlayer = target;
    }

    @Override
    public boolean isBotOnFire() {
        return this.isOnFire();
    }

    private void fallDamageCheck() { // TODO create a better bot event system in the future, also have bot.getAgent()
        if (groundTicks != 0 && noFallTicks == 0 && !(oldVelocity.getY() >= -0.8) && !isFallBlocked()) {
            BotFallDamageEvent event = new BotFallDamageEvent(this, new ArrayList<>(getStandingOn()));

            plugin.getManager().getAgent().onFallDamage(event);

            if (!event.isCancelled()) {
                hurt(damageSources().fall(), (float) Math.pow(3.6, -oldVelocity.getY()));
            }
        }
    }

    private boolean isFallBlocked() {
        AABB box = getBoundingBox();
        double[] xVals = new double[]{
                box.minX,
                box.maxX - 0.01
        };

        double[] zVals = new double[]{
                box.minZ,
                box.maxZ - 0.01
        };
        BoundingBox playerBox = new BoundingBox(box.minX, position().y - 0.01, box.minZ,
                box.maxX, position().y + getBbHeight(), box.maxZ);
        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(getBukkitEntity().getWorld(), Math.floor(x), getLocation().getY(), Math.floor(z));
                Block block = loc.getBlock();
                if (block.getBlockData() instanceof Waterlogged wl && wl.isWaterlogged())
                    return true;
                if (BotUtils.NO_FALL.contains(loc.getBlock().getType()) && (BotUtils.overlaps(playerBox, loc.getBlock().getBoundingBox())
                        || loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.LAVA))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFalling() {
        return velocity.getY() < -0.8;
    }

    @Override
    public void block(int blockLength, int cooldown) {
        if (!shield || blockUse) return;
        startBlocking();
        scheduler.runTaskLater(plugin, () -> stopBlocking(cooldown), blockLength);
    }

    private void startBlocking() {
        this.blocking = true;
        this.blockUse = true;
        startUsingItem(InteractionHand.OFF_HAND);
        //sendPacket(new ClientboundSetEntityDataPacket(getId(), entityData, true));
        sendPacket(new ClientboundSetEntityDataPacket(getId(), entityData.packDirty()));
    }

    private void stopBlocking(int cooldown) {
        this.blocking = false;
        stopUsingItem();
        scheduler.runTaskLater(plugin, () -> this.blockUse = false, cooldown);
        //sendPacket(new ClientboundSetEntityDataPacket(getId(), entityData, true));
        sendPacket(new ClientboundSetEntityDataPacket(getId(), entityData.packDirty()));
    }

    @Override
    public boolean isBotBlocking() {
        return isBlocking();
    }

    @Override
    public void setShield(boolean enabled) {
        this.shield = enabled;

        setItemOffhand(new org.bukkit.inventory.ItemStack(enabled ? Material.SHIELD : Material.AIR));
    }

    private void updateLocation() {
        double y;

        MathUtils.clean(velocity); // TODO lag????

        if (isBotInWater()) {
            y = Math.min(velocity.getY() + 0.1, 0.1);
            addFriction(0.8);
            velocity.setY(y);
        } else {
            if (groundTicks != 0) {
                velocity.setY(0);
                addFriction(0.5);
                y = 0;
            } else {
                y = velocity.getY();
                velocity.setY(Math.max(y - 0.1, -3.5));
            }
        }

        this.move(MoverType.SELF, new Vec3(velocity.getX(), y, velocity.getZ()));
    }

    @Override
    public boolean isBotInWater() {
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

    @Override
    public void jump(Vector vel) {
        if (jumpTicks == 0 && groundTicks > 1) {
            jumpTicks = 4;
            velocity = vel;
        }
    }

    @Override
    public void jump() {
        jump(new Vector(0, 0.5, 0));
    }

    @Override
    public void walk(Vector vel) {
        double max = 0.4;

        Vector sum = velocity.clone().add(vel);
        if (sum.length() > max) sum.normalize().multiply(max);

        velocity = sum;
    }

    @Override
    public void attack(org.bukkit.entity.Entity entity) {
        faceLocation(entity.getLocation());
        punch();

        double damage = ItemUtils.getLegacyAttackDamage(defaultItem);

        if (entity instanceof Damageable) {
            ((Damageable) entity).damage(damage, getBukkitEntity());
        }
    }

    @Override
    public void punch() {
        swing(InteractionHand.MAIN_HAND);
    }

    public boolean checkGround() {
        double vy = velocity.getY();

        if (vy > 0) {
            return false;
        }

        return checkStandingOn();
    }

    public boolean checkStandingOn() {
        World world = getBukkitEntity().getWorld();
        AABB box = getBoundingBox();

        double[] xVals = new double[]{
                box.minX,
                box.maxX
        };

        double[] zVals = new double[]{
                box.minZ,
                box.maxZ
        };
        BoundingBox playerBox = new BoundingBox(box.minX, position().y - 0.01, box.minZ,
                box.maxX, position().y + getBbHeight(), box.maxZ);
        List<Block> standingOn = new ArrayList<>();
        List<Location> locations = new ArrayList<>();

        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(world, x, position().y - 0.01, z);
                Block block = world.getBlockAt(loc);

                if ((LegacyMats.isSolid(block.getType()) || LegacyMats.canStandOn(block.getType())) && BotUtils.overlaps(playerBox, block.getBoundingBox())) {
                    if (!locations.contains(block.getLocation())) {
                        standingOn.add(block);
                        locations.add(block.getLocation());
                    }
                }
            }
        }

        //Fence/wall check
        for (double x : xVals) {
            for (double z : zVals) {
                Location loc = new Location(world, x, position().y - 0.51, z);
                Block block = world.getBlockAt(loc);
                BoundingBox blockBox = loc.getBlock().getBoundingBox();
                BoundingBox modifiedBox = new BoundingBox(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), blockBox.getMaxX(),
                        blockBox.getMinY() + 1.5, blockBox.getMaxZ());

                if ((LegacyMats.FENCE.contains(block.getType()) || LegacyMats.GATES.contains(block.getType()))
                        && LegacyMats.isSolid(block.getType()) && BotUtils.overlaps(playerBox, modifiedBox)) {
                    if (!locations.contains(block.getLocation())) {
                        standingOn.add(block);
                        locations.add(block.getLocation());
                    }
                }
            }
        }

        //Closest block comes first
        Collections.sort(standingOn, (a, b) ->
                Double.compare(BotUtils.getHorizSqDist(a.getLocation(), getLocation()), BotUtils.getHorizSqDist(b.getLocation(), getLocation())));

        this.standingOn = standingOn;
        return !standingOn.isEmpty();
    }

    @Override
    public List<Block> getStandingOn() {
        return standingOn;
    }

    @Override
    public boolean isBotOnGround() {
        return groundTicks != 0;
    }

    @Override
    public void addFriction(double factor) {
        double frictionMin = 0.01;

        double x = velocity.getX();
        double z = velocity.getZ();

        velocity.setX(Math.abs(x) < frictionMin ? 0 : x * factor);
        velocity.setZ(Math.abs(z) < frictionMin ? 0 : z * factor);
    }

    @Override
    public void removeVisually() {
        this.removeTab();
        this.setDead();
    }

    @Override
    public void removeBot() {
        if (Bukkit.isPrimaryThread()) {
            this.remove(RemovalReason.DISCARDED);
        } else {
            scheduler.runTask(plugin, () -> this.remove(RemovalReason.DISCARDED));
        }
        this.removeVisually();
        if (inPlayerList)
            this.server.getPlayerList().getPlayers().remove(this);
    }

    private void removeTab() {
        sendPacket(new ClientboundPlayerInfoRemovePacket(Arrays.asList(this.getUUID())));
    }

    public void setRemoveOnDeath(boolean enabled) {
        this.removeOnDeath = enabled;
    }

    private void setDead() {
        sendPacket(new ClientboundRemoveEntitiesPacket(getId()));

        this.dead = true;
        this.inventoryMenu.removed(this);
        if (this.containerMenu != null) {
            this.containerMenu.removed(this);
        }
    }

    private void dieCheck() {
        if (removeOnDeath) {

            // I replaced HashSet with ConcurrentHashMap.newKeySet which creates a "ConcurrentHashSet"
            // this should fix the concurrentmodificationexception mentioned above, I used the ConcurrentHashMap.newKeySet to make a "ConcurrentHashSet"
            plugin.getManager().remove(this);

            scheduler.runTaskLater(plugin, this::removeBot, 20);

            this.removeTab();
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        this.dieCheck();
    }

    @Override
    public void push(Entity entity) {
        if (!this.isPassengerOfSameVehicle(entity) && !entity.noPhysics && !this.noPhysics) {
            double d0 = entity.getX() - this.getZ();
            double d1 = entity.getX() - this.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.009999999776482582D) {
                d2 = Math.sqrt(d2);
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

                if (!this.isVehicle()) {
                    velocity.add(new Vector(-d0, 0.0D, -d1));
                }

                if (!entity.isVehicle()) {
                    entity.push(d0, 0.0D, d1);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        Entity attacker = damagesource.getEntity();

        float damage;

        boolean playerInstance = attacker instanceof ServerPlayer;

        Player killer;

        if (playerInstance) {
            killer = ((ServerPlayer) attacker).getBukkitEntity();

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

        boolean damaged = super.hurt(damagesource, damage);

        if (!damaged && blocking) {
            getBukkitEntity().getWorld().playSound(getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
        }

        if (damaged && attacker != null) {
            if (playerInstance && !isAlive()) {
                agent.onBotKilledByPlayer(new BotKilledByPlayerEvent(this, killer));

            } else {
                kb(getLocation(), attacker.getBukkitEntity().getLocation(), attacker);
            }
        }

        return damaged;
    }

    private void kb(Location loc1, Location loc2, Entity attacker) {
        Vector vel = loc1.toVector().subtract(loc2.toVector()).setY(0).normalize().multiply(0.3);

        if (isBotOnGround()) vel.multiply(0.8).setY(0.4);
        if (attacker.getBukkitEntity() instanceof Player && ((Player) attacker.getBukkitEntity()).getInventory().getItemInMainHand().getItemMeta() != null) {
            if (((Player) attacker.getBukkitEntity()).getInventory().getItemInMainHand().getItemMeta().hasEnchant(Enchantment.KNOCKBACK) && attacker.getBukkitEntity() instanceof Player) {
                int kbLevel = ((Player) attacker.getBukkitEntity()).getInventory().getItemInMainHand().getItemMeta().getEnchants().get(Enchantment.KNOCKBACK);
                if (kbLevel == 1) {
                    vel.multiply(1.05).setY(.4);
                } else {
                    vel.multiply(1.9).setY(.4);
                }
            }
        }
        velocity = vel;
    }

    @Override
    public int getKills() {
        return kills;
    }

    @Override
    public void incrementKills() {
        kills++;
    }

    @Override
    public Location getLocation() {
        return getBukkitEntity().getLocation();
    }

    @Override
    public BoundingBox getBotBoundingBox() {
        return getBukkitEntity().getBoundingBox();
    }

    @Override
    public void setBotPitch(float pitch) {
        super.setXRot(pitch);
    }

    @Override
    public void faceLocation(Location loc) {
        look(loc.toVector().subtract(getLocation().toVector()), false);
    }

    @Override
    public void look(BlockFace face) {
        look(face.getDirection(), face == BlockFace.DOWN || face == BlockFace.UP);
    }

    private void look(Vector dir, boolean keepYaw) {
        float yaw, pitch;

        if (keepYaw) {
            yaw = this.getYRot();
            pitch = MathUtils.fetchPitch(dir);
        } else {
            float[] vals = MathUtils.fetchYawPitch(dir);
            yaw = vals[0];
            pitch = vals[1];

            sendPacket(new ClientboundRotateHeadPacket(getBukkitEntity().getHandle(), (byte) (yaw * 256 / 360f)));
        }

        setRot(yaw, pitch);
    }

    @Override
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

        if (!LegacyMats.isSolid(block.getType())) {
            block.setType(type);
            if (world != null) world.playSound(loc, Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Override
    public void setItem(org.bukkit.inventory.ItemStack item) {
        setItem(item, EquipmentSlot.MAINHAND);
    }

    @Override
    public void setItemOffhand(org.bukkit.inventory.ItemStack item) {
        setItem(item, EquipmentSlot.OFFHAND);
    }

    @Override
    public void setItem(ItemStack item, org.bukkit.inventory.EquipmentSlot slot) {
        EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        setItem(item, nmsSlot);
    }

    public void setItem(org.bukkit.inventory.ItemStack item, EquipmentSlot slot) {
        if (item == null) item = defaultItem;

        //System.out.println("set");
        if (slot == EquipmentSlot.MAINHAND) {
            getBukkitEntity().getInventory().setItemInMainHand(item);
        } else if (slot == EquipmentSlot.OFFHAND) {
            getBukkitEntity().getInventory().setItemInOffHand(item);
        }

        //System.out.println("slot = " + slot);
        //System.out.println("item = " + item);
        sendPacket(new ClientboundSetEquipmentPacket(getId(), new ArrayList<>(Collections.singletonList(
                new Pair<>(slot, CraftItemStack.asNMSCopy(item))
        ))));
    }

    @Override
    public void swim() {
        getBukkitEntity().setSwimming(true);
        registerPose(Pose.SWIMMING);
    }

    @Override
    public void sneak() {
        getBukkitEntity().setSneaking(true);
        registerPose(Pose.CROUCHING);
    }

    @Override
    public void stand() {
        Player player = getBukkitEntity();
        player.setSneaking(false);
        player.setSwimming(false);

        registerPose(Pose.STANDING);
    }

    private void registerPose(Pose pose) {
        //entityData.set(new EntityDataAccessor<>(6, EntityDataSerializers.POSE), pose);
        //sendPacket(new ClientboundSetEntityDataPacket(getId(), entityData, false));
    }

    @Override
    public void doTick() {
        detectEquipmentUpdatesPublic();
        baseTick();
    }

    @Override
    public boolean isInPlayerList() {
        return inPlayerList;
    }

    @Override
    public World.Environment getDimension() {
        return getBukkitEntity().getWorld().getEnvironment();
    }
}
