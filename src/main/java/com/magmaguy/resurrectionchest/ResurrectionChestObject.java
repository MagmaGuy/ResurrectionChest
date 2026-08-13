package com.magmaguy.resurrectionchest;

import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
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
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ResurrectionChestObject implements PersistentObject {
    private static final BlockFace[] CARDINAL_FACES = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
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
    private List<Location> knownChestLocations = new ArrayList<>();
    private List<Location> allBlocks = null;
    private List<Location> allSigns = null;
    private boolean initialTrackingPending = false;
    private boolean legacyTrackingMigrationPending = false;
    private String modelName = "none";
    public ResurrectionChestObject(Player player, Location location) {
        this.uuid = player.getUniqueId();
        this.location = location;
        this.initialTrackingPending = true;
        if (CustomModel.FMMIsEnabled()) this.modelName = determineModelName(player);
        resurrectionChests.put(uuid, this);
        setIsDoubleChest();
        calculateCenterLocation();
        spawnCustomModel();
        // Block marking is deferred to the tick after the SignChangeEvent (see
        // DeathChestConstructor): force-updating the sign's tile state mid-event
        // would discard the text the event is about to apply, and the missing tag
        // is how DeathChestRemover tells the creation event apart from later edits.
        PlayerDataConfig.addPlayerdata(uuid, location, modelName);
        worldName = location.getWorld().getName();
        persistentObjectHandler = new PersistentObjectHandler(this);
        isLoaded = true;
    }

    public ResurrectionChestObject(UUID uuid,
                                   String locationString,
                                   String modelName,
                                   int trackedBlockSchemaVersion) {
        this.uuid = uuid;
        if (modelName != null) this.modelName = modelName;
        this.legacyTrackingMigrationPending =
                trackedBlockSchemaVersion < PlayerDataConfig.TRACKED_BLOCK_SCHEMA_VERSION;

        Location parsedLocation = LocationParser.parseLocation(locationString);
        this.location = parsedLocation;
        worldName = parsedLocation.getWorld() != null ? parsedLocation.getWorld().getName() : LocationParser.getWorldString(locationString);
        resurrectionChests.put(uuid, this);
        boolean validLoadedRegistration = true;
        if (locationWorldAndChunkAreLoaded()) {
            loadChestState();
            validLoadedRegistration = authorizeLoadedTrackedChest();
            if (validLoadedRegistration) {
                spawnCustomModel();
                isLoaded = true;
            }
        } else {
            isLoaded = false;
        }
        persistentObjectHandler = new PersistentObjectHandler(this);
        if (!validLoadedRegistration) invalidateRegistration("stored chest ownership tags are missing or conflicting");
    }

    public static void startClock() {
        if (gameClock != null) {
            gameClock.cancel();
            gameClock = null;
        }
        if (!DefaultConfig.enableParticleEffects) return;
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

    public static void refreshAllModels() {
        for (ResurrectionChestObject resurrectionChestObject : new ArrayList<>(resurrectionChests.values())) {
            resurrectionChestObject.refreshCustomModel();
        }
    }

    public static ResurrectionChestObject getResurrectionChest(Player player) {
        if (!resurrectionChests.containsKey(player.getUniqueId()))
            return null;
        return resurrectionChests.get(player.getUniqueId());
    }

    public boolean isCurrentRegistration() {
        return resurrectionChests.get(uuid) == this;
    }

    public boolean isTrackingClaimPending() {
        return initialTrackingPending && isCurrentRegistration();
    }

    public static ResurrectionChestObject getResurrectionChest(Location location) {
        if (location == null || location.getWorld() == null) return null;
        for (ResurrectionChestObject resurrectionChestObject : resurrectionChests.values()) {
            if (!resurrectionChestObject.isLoaded) continue;
            for (Location blockLocation : resurrectionChestObject.getAllBlocks()) {
                if (sameBlock(location, blockLocation))
                    return resurrectionChestObject;
            }
        }
        return null;
    }

    private static boolean sameBlock(Location first, Location second) {
        if (first == null || second == null ||
                first.getWorld() == null || second.getWorld() == null)
            return false;
        return first.getWorld().getUID().equals(second.getWorld().getUID()) &&
                first.getBlockX() == second.getBlockX() &&
                first.getBlockY() == second.getBlockY() &&
                first.getBlockZ() == second.getBlockZ();
    }

    public static void initializeConfigDeathchests() {
        for (String uuidString : new ArrayList<>(
                PlayerDataConfig.getInstance().getFileConfiguration().getKeys(false))) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                PlayerDataConfig.RawPlayerData rawData = PlayerDataConfig.getRawPlayerData(uuid);
                new ResurrectionChestObject(
                        uuid,
                        rawData.locationString(),
                        rawData.chestModel(),
                        rawData.trackedBlockSchemaVersion());
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
        doubleChest = false;
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

    private boolean locationWorldAndChunkAreLoaded() {
        if (location == null || location.getWorld() == null) return false;
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private void loadChestState() {
        clearCachedBlocks();
        setIsDoubleChest();
        calculateCenterLocation();
    }

    public boolean markTrackedBlocks() {
        if (!isCurrentRegistration() || !locationWorldAndChunkAreLoaded()) return false;

        List<Block> chestBlocks = getAllChests().stream()
                .map(Location::getBlock)
                .toList();
        List<Block> signBlocks = getAllSigns().stream()
                .map(Location::getBlock)
                .toList();
        if (chestBlocks.isEmpty() || chestBlocks.stream().anyMatch(block ->
                block.getType() != Material.CHEST ||
                        trackingState(block, "chest") == TrackingState.CONFLICT))
            return false;
        if (signBlocks.stream().anyMatch(block ->
                !block.getType().toString().endsWith("_SIGN") ||
                        trackingState(block, "sign") == TrackingState.CONFLICT))
            return false;

        for (Block block : chestBlocks) {
            if (!markTrackedBlock(block, "chest")) return false;
        }
        for (Block block : signBlocks) {
            if (!markTrackedBlock(block, "sign")) return false;
        }
        initialTrackingPending = false;
        legacyTrackingMigrationPending = false;
        PlayerDataConfig.markTrackedBlockSchemaCurrent(uuid);
        return true;
    }

    private boolean markTrackedBlock(Block block, String type) {
        if (!(block.getState() instanceof TileState tileState)) return false;
        tileState.getPersistentDataContainer().set(ownerKey(), org.bukkit.persistence.PersistentDataType.STRING, uuid.toString());
        tileState.getPersistentDataContainer().set(blockTypeKey(), org.bukkit.persistence.PersistentDataType.STRING, type);
        return tileState.update(true, false);
    }

    public boolean isTrackedSign(Block block) {
        if (initialTrackingPending) return false;
        return mayTrustTrackedLocation(block, "sign");
    }

    /**
     * Returns whether this block is still a chest owned by this registration.
     * Strict mode requires the owner/type PDC written when the chest was
     * registered. The existing high-compatibility mode deliberately falls back
     * to location/material trust for servers whose block metadata is stripped.
     */
    public boolean isTrackedChest(Block block) {
        return block != null && block.getType() == Material.CHEST &&
                mayTrustTrackedLocation(block, "chest");
    }

    /**
     * Validates every half of the inventory that would receive a player's
     * items. This prevents a chest destroyed or replaced outside Bukkit's
     * normal break event from inheriting the old registration.
     */
    public boolean hasUsableTrackedChest() {
        if (resurrectionChests.get(uuid) != this ||
                !locationWorldAndChunkAreLoaded())
            return false;

        Block primaryBlock = location.getBlock();
        if (!isTrackedChest(primaryBlock) || !(primaryBlock.getState() instanceof Chest chest))
            return false;

        if (!(chest.getInventory() instanceof DoubleChestInventory doubleChestInventory))
            return true;

        return isTrackedChest(doubleChestInventory.getLeftSide().getLocation().getBlock()) &&
                isTrackedChest(doubleChestInventory.getRightSide().getLocation().getBlock());
    }

    private boolean mayTrustTrackedLocation(Block block, String type) {
        TrackingState state = trackingState(block, type);
        if (state == TrackingState.MATCH) return true;
        if (state == TrackingState.CONFLICT) return false;
        return DefaultConfig.enableHighCompatibility;
    }

    private TrackingState trackingState(Block block, String type) {
        if (block == null || !(block.getState() instanceof TileState tileState))
            return TrackingState.ABSENT;
        String owner = tileState.getPersistentDataContainer().get(
                ownerKey(), org.bukkit.persistence.PersistentDataType.STRING);
        String blockType = tileState.getPersistentDataContainer().get(
                blockTypeKey(), org.bukkit.persistence.PersistentDataType.STRING);
        if (owner == null && blockType == null) return TrackingState.ABSENT;
        if (uuid.toString().equals(owner) && type.equals(blockType)) return TrackingState.MATCH;
        return TrackingState.CONFLICT;
    }

    public static boolean hasTrackedChestConflict(Block block, UUID prospectiveOwner) {
        if (block == null || !(block.getState() instanceof TileState tileState)) return false;
        String owner = tileState.getPersistentDataContainer().get(
                ownerKey(), org.bukkit.persistence.PersistentDataType.STRING);
        String blockType = tileState.getPersistentDataContainer().get(
                blockTypeKey(), org.bukkit.persistence.PersistentDataType.STRING);
        if (owner == null && blockType == null) return false;
        return !prospectiveOwner.toString().equals(owner) || !"chest".equals(blockType);
    }

    private boolean authorizeLoadedTrackedChest() {
        if (initialTrackingPending) return true;
        if (legacyTrackingMigrationPending) return markTrackedBlocks();
        return hasUsableTrackedChest();
    }

    private void invalidateRegistration(String reason) {
        isLoaded = false;
        Logger.warn("Discarding ResurrectionChest registration for " + uuid + " at " +
                LocationParser.serializeLocation(location) + ": " + reason + ".");
        remove();
    }

    private enum TrackingState {
        MATCH,
        ABSENT,
        CONFLICT
    }

    private static NamespacedKey ownerKey() {
        return new NamespacedKey(MetadataHandler.PLUGIN, "resurrectionchest_owner");
    }

    private static NamespacedKey blockTypeKey() {
        return new NamespacedKey(MetadataHandler.PLUGIN, "resurrectionchest_block_type");
    }

    private void clearCachedBlocks() {
        allChests = null;
        allBlocks = null;
        allSigns = null;
    }

    public void markChestChanged() {
        if (!isCurrentRegistration()) return;
        clearCachedBlocks();
        // Re-tag after clearing so a newly joined double-chest half (and its signs)
        // gets the tracking tag; without this, strict mode skips the untagged half
        // in clearTrackedBlocks/removeAttachedSigns. Idempotent and chunk-guarded.
        if (!markTrackedBlocks()) {
            invalidateRegistration("a joined chest or sign has conflicting ownership tags");
            return;
        }
        refreshCustomModel();
    }

    public List<Location> getAllSigns() {
        if (allSigns != null) return allSigns;
        List<Location> signs = new ArrayList<>();
        for (Location chestLocation : getAllChests()) {
            Block chestBlock = chestLocation.getBlock();
            for (BlockFace face : CARDINAL_FACES) {
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
        if (allChests != null) return allChests;
        BlockState chestState = location.getBlock().getState();
        if (chestState instanceof Chest && ((Chest) chestState).getInventory() instanceof DoubleChestInventory doubleChestInventory) {
            return rememberChestLocations(List.of(
                    doubleChestInventory.getLeftSide().getLocation(),
                    doubleChestInventory.getRightSide().getLocation()));
        }
        if (chestState instanceof Chest)
            return rememberChestLocations(List.of(location));
        if (!knownChestLocations.isEmpty())
            return allChests = new ArrayList<>(knownChestLocations);
        return allChests = List.of(location);
    }

    private List<Location> rememberChestLocations(List<Location> chestLocations) {
        knownChestLocations = chestLocations.stream()
                .map(Location::clone)
                .toList();
        return allChests = chestLocations;
    }

    public List<Location> getAllBlocks() {
        if (allBlocks != null) return allBlocks;
        List<Location> locations = new ArrayList<>();
        locations.addAll(getAllSigns());
        locations.addAll(getAllChests());
        return allBlocks = locations;
    }

    /**
     * The model should not survive ResurrectionChest lifecycle changes. It is
     * recreated from playerData/chest state when the chunk loads again.
     */
    private void spawnCustomModel() {
        if (!CustomModel.FMMIsEnabled()) return;
        if (modelName.equals("none")) return;
        String finalModelName = modelName + (isDoubleChest() ? "_double" : "_single");
        customModel = CustomModel.CreateChestProp(centerLocation, this, finalModelName);
    }

    private void removeCustomModel() {
        if (customModel == null) return;
        customModel.remove();
        customModel = null;
    }

    public void refreshCustomModel() {
        if (!locationWorldAndChunkAreLoaded()) {
            removeCustomModel();
            isLoaded = false;
            return;
        }

        loadChestState();
        if (!authorizeLoadedTrackedChest()) {
            invalidateRegistration("model refresh found missing or conflicting chest ownership tags");
            return;
        }
        removeCustomModel();
        spawnCustomModel();
    }

    private void tick() {
        //prevent ticking if the chunk is not loaded
        if (!locationWorldAndChunkAreLoaded()) {
            isLoaded = false;
            return;
        }
        if (!isLoaded) return;
        doParticleEffects();
    }

    private void doParticleEffects() {
        Location adjustedLocation = centerLocation.clone().add(0, 0.5, 0);
        location.getWorld().spawnParticle(DefaultConfig.resolvedParticleEffect, adjustedLocation, 1, 0.075, 0.075, 0.75, 0.8);
    }

    /**
     * Removes tracked signs attached to every known half of the chest. Cached
     * locations intentionally remain usable after a chest block disappears, so
     * the missing-chest cleanup path cannot leave orphaned signs behind.
     */
    private void removeAttachedSigns() {
        if (location == null || location.getWorld() == null) return;

        clearCachedBlocks();
        for (Location signLoc : getAllSigns()) {
            Block signBlock = signLoc.getBlock();
            if (!(signBlock.getBlockData() instanceof WallSign)) continue;
            if (!mayTrustTrackedLocation(signBlock, "sign")) continue;
            signBlock.setType(Material.AIR);
        }
    }

    public void remove() {
        PlayerDataConfig.unregisterDeathChestEntry(this);
        if (persistentObjectHandler != null) persistentObjectHandler.remove();

        // Remove any signs attached to the chest
        removeAttachedSigns();

        resurrectionChests.remove(uuid);
        removeCustomModel();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', DefaultConfig.deathChestRemovedMessage));
    }

    public static int clearAllTrackedBlocks() {
        int removedBlocks = 0;
        for (ResurrectionChestObject resurrectionChestObject : new ArrayList<>(resurrectionChests.values())) {
            removedBlocks += resurrectionChestObject.clearTrackedBlocks();
        }
        return removedBlocks;
    }

    private int clearTrackedBlocks() {
        if (location == null || location.getWorld() == null) {
            PlayerDataConfig.removePlayerData(uuid);
            resurrectionChests.remove(uuid);
            if (persistentObjectHandler != null) persistentObjectHandler.remove();
            removeCustomModel();
            return 0;
        }

        location.getWorld().getChunkAt(location);

        int removedBlocks = 0;
        for (Location signLocation : new ArrayList<>(getAllSigns())) {
            Block signBlock = signLocation.getBlock();
            if (!signBlock.getType().toString().endsWith("_SIGN")) continue;
            if (mayTrustTrackedLocation(signBlock, "sign")) {
                signBlock.setType(Material.AIR);
                removedBlocks++;
            }
        }

        boolean inventoryDropped = false;
        for (Location chestLocation : new ArrayList<>(getAllChests())) {
            Block chestBlock = chestLocation.getBlock();
            if (chestBlock.getType() != Material.CHEST) continue;
            if (!mayTrustTrackedLocation(chestBlock, "chest")) continue;
            if (!inventoryDropped) {
                dropChestInventory(chestBlock);
                inventoryDropped = true;
            }
            chestBlock.setType(Material.AIR);
            removedBlocks++;
        }

        PlayerDataConfig.removePlayerData(uuid);
        if (persistentObjectHandler != null) persistentObjectHandler.remove();
        removeCustomModel();
        resurrectionChests.remove(uuid);
        return removedBlocks;
    }

    private void dropChestInventory(Block chestBlock) {
        if (!(chestBlock.getState() instanceof Chest chest)) return;
        Inventory inventory = chest.getInventory();
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            chestBlock.getWorld().dropItemNaturally(chestBlock.getLocation(), itemStack);
        }
        inventory.clear();
    }

    @Override
    public void chunkLoad() {
        if (!locationWorldAndChunkAreLoaded()) {
            isLoaded = false;
            return;
        }
        if (isLoaded && customModel != null) {
            if (!authorizeLoadedTrackedChest()) {
                invalidateRegistration("loaded chest ownership tags are missing or conflicting");
                return;
            }
            customModel.refreshPropBlocks();
            return;
        }
        loadChestState();
        if (!authorizeLoadedTrackedChest()) {
            invalidateRegistration("loaded chest ownership tags are missing or conflicting");
            return;
        }
        spawnCustomModel();
        if (customModel != null) customModel.refreshPropBlocks();
        isLoaded = true;
    }

    private boolean isLoaded = false;

    @Override
    public void chunkUnload() {
        isLoaded = false;
        removeCustomModel();
    }

    @Override
    public void worldLoad(World world) {
        location.setWorld(world);
        chunkLoad();
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
