package com.magmaguy.resurrectionchest;

import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import com.magmaguy.magmacore.util.ChunkLocationChecker;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.configs.DefaultConfig;
import com.magmaguy.resurrectionchest.configs.PlayerDataConfig;
import com.magmaguy.resurrectionchest.thirdparty.CustomModel;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ResurrectionChestObject implements PersistentObject {
    @Getter
    private static final HashMap<UUID, ResurrectionChestObject> resurrectionChests = new HashMap<>();
    private static BukkitTask gameClock = null;
    @Getter
    private final UUID uuid;
    @Getter
    private final Location location;
    private final String worldName;
    private final PersistentObjectHandler persistentObjectHandler;
    private Location centerLocation;
    private CustomModel customModel = null;
    @Getter
    private boolean doubleChest = false;
    private List<Location> allChests = null;
    private List<Location> allBlocks = null;
    private List<Location> allSigns = null;
    private boolean chestHasChanged = false;
    @Getter
    private String modelName = "none";
    public ResurrectionChestObject(Player player, Location location) {
        this.uuid = player.getUniqueId();
        this.location = location;
        if (CustomModel.FMMIsEnabled()) this.modelName = determineModelName(player);
        resurrectionChests.put(uuid, this);
        setIsDoubleChest();
        calculateCenterLocation();
        spawnCustomModel();
        PlayerDataConfig.addPlayerdata(uuid, location, modelName);
        worldName = location.getWorld().getName();
        persistentObjectHandler = new PersistentObjectHandler(this);
        isLoaded = true;
    }

    public ResurrectionChestObject(UUID uuid, Location location, String modelName) {
        this.uuid = uuid;
        this.location = location;
        this.modelName = modelName != null ? modelName : "none";
        resurrectionChests.put(uuid, this);
        setIsDoubleChest();
        calculateCenterLocation();
        spawnCustomModel();
        worldName = location.getWorld().getName();
        persistentObjectHandler = new PersistentObjectHandler(this);
        isLoaded = true;
    }

    public ResurrectionChestObject(UUID uuid, String locationString, String modelName) {
        this.uuid = uuid;
        if (modelName != null) this.modelName = modelName;

        Location parsedLocation = LocationParser.parseLocation(locationString);
        if (parsedLocation.getWorld() != null) {
            // World is loaded, initialize fully
            this.location = parsedLocation;
            resurrectionChests.put(uuid, this);
            setIsDoubleChest();
            calculateCenterLocation();
            spawnCustomModel();
            worldName = location.getWorld().getName();
            isLoaded = true;
        } else {
            // World not loaded, initialize minimal
            this.location = parsedLocation; // Keep the location even if world is null
            resurrectionChests.put(uuid, this);
            setIsDoubleChest();
            worldName = LocationParser.getWorldString(locationString);
            isLoaded = false;
        }
        persistentObjectHandler = new PersistentObjectHandler(this);
    }

    public static void startClock() {
        if (gameClock != null) gameClock.cancel();
        gameClock = new BukkitRunnable() {
            @Override
            public void run() {
                for (ResurrectionChestObject resurrectionChestObject : resurrectionChests.values())
                    resurrectionChestObject.tick();
            }
        }.runTaskTimer(ResurrectionChest.plugin, 0, 1);
    }

    public static void shutdown() {
        if (gameClock != null) {
            gameClock.cancel();
            gameClock = null;
        }
    }

    public static ResurrectionChestObject getResurrectionChest(Player player) {
        if (!resurrectionChests.containsKey(player.getUniqueId()))
            return null;
        return resurrectionChests.get(player.getUniqueId());
    }

    public static ResurrectionChestObject getResurrectionChest(Location location) {
        for (ResurrectionChestObject resurrectionChestObject : resurrectionChests.values()) {
            if (!resurrectionChestObject.isLoaded) continue;
            for (Location blockLocation : resurrectionChestObject.getAllBlocks()) {
                if (location.getBlockX() == blockLocation.getBlockX() &&
                        location.getBlockY() == blockLocation.getBlockY() &&
                        location.getBlockZ() == blockLocation.getBlockZ())
                    return resurrectionChestObject;
            }
        }
        return null;
    }

    public static void initializeConfigDeathchests() {
        for (String uuidString : PlayerDataConfig.getInstance().getFileConfiguration().getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                PlayerDataConfig.PlayerData playerData = PlayerDataConfig.getPlayerData(uuid);
                new ResurrectionChestObject(uuid, playerData.location().toString(), playerData.chestModel());
            } catch (Exception e) {
                Logger.warn("Failed to load resurrection chest for player " + uuidString + ": " + e.getMessage());
            }
        }
    }

    private String determineModelName(Player player) {
        if (player.hasPermission("resurrectionchest.model.premium") && FileModelConverter.getConvertedFileModels().get(DefaultConfig.premiumSingleDeathChestModelName) != null) {
            return "resurrectionchest_angelic";
        } else if (player.hasPermission("resurrectionchest.model.free")) {
            return "resurrectionchest_free";
        }
        return "none";
    }

    private void setIsDoubleChest() {
        BlockState chestState = location.getBlock().getState();
        if (chestState instanceof Chest && ((Chest) chestState).getInventory() instanceof DoubleChestInventory)
            doubleChest = true;
    }

    private void calculateCenterLocation() {
        BlockState chestState = location.getBlock().getState();

        if (chestState instanceof Chest chest) {
            if (chest.getInventory() instanceof DoubleChestInventory doubleChestInventory) {
                // Get the locations of both chest halves
                Location leftLocation = doubleChestInventory.getLeftSide().getLocation();
                Location rightLocation = doubleChestInventory.getRightSide().getLocation();

                // For double chests, calculate the true midpoint
                double midX = (leftLocation.getX() + rightLocation.getX()) / 2.0;
                double midY = (leftLocation.getY() + rightLocation.getY()) / 2.0;
                double midZ = (leftLocation.getZ() + rightLocation.getZ()) / 2.0;

                centerLocation = new Location(leftLocation.getWorld(), midX, midY, midZ);
                // Only add 0.5 to Y for vertical centering - X and Z are already centered between blocks
                centerLocation.add(0.5, 0, 0.5);
                centerLocation.setYaw(location.getYaw());
            } else {
                // Single chest - center it within the block
                centerLocation = location.clone().add(0.5, 0, 0.5);
            }
        } else {
            // Not a chest - just center the block
            centerLocation = location.clone().add(0.5, 0, 0.5);
        }
    }

    public void setChestHasChanged(boolean chestHasChanged) {
        this.chestHasChanged = chestHasChanged;
        calculateCenterLocation();
        setIsDoubleChest();
        if (customModel != null) {
            customModel.remove();
            spawnCustomModel();
        }
    }

    public List<Location> getAllSigns() {
        if (allSigns != null && !chestHasChanged) return allSigns;
        List<Location> signs = new ArrayList<>();
        for (Location chestLocation : getAllChests()) {
            Block chestBlock = chestLocation.getBlock();
            for (BlockFace face : BlockFace.values()) {
                Block block = chestBlock.getRelative(face);
                if (block.getType().toString().endsWith("WALL_SIGN")) {
                    BlockData blockData = block.getBlockData();
                    if (blockData instanceof WallSign wallSign) {
                        BlockFace attachedFace = wallSign.getFacing();
                        if (attachedFace == face) {
                            signs.add(block.getLocation());
                        }
                    }
                }
            }
        }

        return allSigns = signs;
    }

    public List<Location> getAllChests() {
        if (allChests != null && !chestHasChanged) return allChests;
        if (!isDoubleChest()) {
            return allChests = List.of(location);
        }
        BlockState chestState = location.getBlock().getState();
        if (chestState instanceof Chest && ((Chest) chestState).getInventory() instanceof DoubleChestInventory doubleChestInventory) {
            return allChests = List.of(doubleChestInventory.getLeftSide().getLocation(), doubleChestInventory.getRightSide().getLocation());
        }
        return allChests = List.of(location);
    }

    public List<Location> getAllBlocks() {
        if (allBlocks != null && !chestHasChanged) return allBlocks;
        List<Location> locations = new ArrayList<>();
        locations.addAll(getAllSigns());
        locations.addAll(getAllChests());
        return allBlocks = locations;
    }

    /**
     * The model isn't manually removed on chunk unload because it is not persistent
     */
    private void spawnCustomModel() {
        if (!CustomModel.FMMIsEnabled()) return;
        if (customModel != null) customModel.remove();
        if (modelName.equals("none")) return;
        String finalModelName = modelName + (isDoubleChest() ? "_double" : "_single");
        customModel = CustomModel.CreateChestProp(centerLocation, uuid, this, finalModelName);
    }

    private void tick() {
        if (!DefaultConfig.enableParticleEffects) return;
        //prevent ticking if the chunk is not loaded
        if (!isLoaded)             return;
        doParticleEffects();
    }

    private void doParticleEffects() {
        Location adjustedLocation = centerLocation.clone().add(0, 0.5, 0);
        location.getWorld().spawnParticle(Particle.valueOf(DefaultConfig.particleEffect3), adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
    }

    /**
     * Removes any signs attached to the chest block
     */
    private void removeAttachedSigns() {
        if (location == null || location.getWorld() == null) return;

        Block chestBlock = location.getBlock();
        if (chestBlock.getType() != Material.CHEST) return;

        // Check all 4 sides of the chest for attached wall signs
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (BlockFace face : faces) {
            Block adjacentBlock = chestBlock.getRelative(face);

            // Check if it's a wall sign
            if (adjacentBlock.getType().toString().endsWith("_WALL_SIGN")) {

                // Check if the sign is actually attached to our chest
                if (adjacentBlock.getBlockData() instanceof WallSign wallSign) {
                    Block attachedBlock = adjacentBlock.getRelative(wallSign.getFacing().getOppositeFace());

                    // If the sign is attached to our chest block, remove it
                    if (attachedBlock.equals(chestBlock)) {
                        adjacentBlock.setType(Material.AIR);
                    }
                }
            }
        }
    }

    public void remove() {
        PlayerDataConfig.unregisterDeathChestEntry(this);
        if (persistentObjectHandler != null) persistentObjectHandler.remove();

        // Remove any signs attached to the chest
        removeAttachedSigns();

        resurrectionChests.remove(uuid);
        if (customModel != null) customModel.remove();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) Logger.sendMessage(player, DefaultConfig.deathChestRemovedMessage);
    }

    @Override
    public void chunkLoad() {
        spawnCustomModel();
        isLoaded = true;
    }

    private boolean isLoaded = false;

    @Override
    public void chunkUnload() {
        isLoaded = false;
    }

    @Override
    public void worldLoad(World world) {
        chunkLoad();
        location.setWorld(world);
    }

    @Override
    public void worldUnload() {
        chunkUnload();
        location.setWorld(null);
    }

    @Override
    public Location getPersistentLocation() {
        return location;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }
}