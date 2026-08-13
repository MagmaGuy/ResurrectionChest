package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PersistentObjectHandler {
    private static final Map<String, Set<PersistentObjectHandler>> WORLD_OBJECTS = new HashMap<>();
    private static final Map<ChunkKey, Set<PersistentObjectHandler>> CHUNK_OBJECTS = new HashMap<>();

    private final PersistentObject persistentObject;
    private final String worldName;
    private Location persistentLocation;
    private ChunkKey chunkKey;
    private boolean registeredByWorld;
    private boolean removed;

    /**
     * Tracks a persistent object (a resurrection chest) so it can be reloaded
     * when its chunk or world loads again.
     *
     * @param persistentObject Persistent object to track
     */
    public PersistentObjectHandler(PersistentObject persistentObject) {
        this.persistentObject = persistentObject;
        this.persistentLocation = persistentObject.getPersistentLocation();
        this.worldName = persistentObject.getWorldName();
        registerForCurrentState();
    }

    /**
     * Clears all data for a correct shutdown.
     */
    public static void shutdown() {
        WORLD_OBJECTS.clear();
        CHUNK_OBJECTS.clear();
    }

    private void registerForCurrentState() {
        if (removed) return;
        if (persistentLocation != null && persistentLocation.getWorld() != null &&
                Bukkit.getWorld(persistentLocation.getWorld().getUID()) != null) {
            registerByChunk();
        } else {
            registerByWorld();
        }
    }

    private void registerByChunk() {
        if (removed) return;
        if (persistentLocation == null || persistentLocation.getWorld() == null) {
            registerByWorld();
            return;
        }
        chunkKey = ChunkKey.from(persistentLocation);
        CHUNK_OBJECTS.computeIfAbsent(chunkKey, ignored -> new HashSet<>()).add(this);
        registeredByWorld = false;
    }

    private void registerByWorld() {
        if (removed) return;
        chunkKey = null;
        WORLD_OBJECTS.computeIfAbsent(worldName, ignored -> new HashSet<>()).add(this);
        registeredByWorld = true;
    }

    private void unregister() {
        if (chunkKey != null) {
            removeFromBucket(CHUNK_OBJECTS, chunkKey, this);
            chunkKey = null;
        }
        if (registeredByWorld) {
            removeFromBucket(WORLD_OBJECTS, worldName, this);
            registeredByWorld = false;
        }
    }

    private static <K> void removeFromBucket(Map<K, Set<PersistentObjectHandler>> index, K key,
                                             PersistentObjectHandler handler) {
        Set<PersistentObjectHandler> bucket = index.get(key);
        if (bucket == null) return;
        bucket.remove(handler);
        if (bucket.isEmpty()) index.remove(key);
    }

    public void worldLoad(World world) {
        if (removed) return;
        unregister();
        try {
            persistentObject.worldLoad(world);
        } finally {
            persistentLocation = persistentObject.getPersistentLocation();
            registerForCurrentState();
        }
    }

    public void worldUnload() {
        if (removed) return;
        unregister();
        try {
            persistentObject.worldUnload();
        } finally {
            persistentLocation = persistentObject.getPersistentLocation();
            registerByWorld();
        }
    }

    public void updatePersistentLocation(Location location) {
        if (removed) return;
        unregister();
        persistentLocation = location;
        registerForCurrentState();
    }

    public void remove() {
        removed = true;
        unregister();
    }

    private void loadIfActive() {
        if (removed) return;
        persistentObject.chunkLoad();
    }

    private record ChunkKey(UUID worldId, int x, int z) {
        private static ChunkKey from(Chunk chunk) {
            return new ChunkKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        }

        private static ChunkKey from(Location location) {
            return new ChunkKey(location.getWorld().getUID(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        }
    }

    public static class PersistentObjectHandlerEvents implements Listener {
        private static List<PersistentObjectHandler> chunkHandlers(Chunk chunk) {
            Set<PersistentObjectHandler> handlers = CHUNK_OBJECTS.get(ChunkKey.from(chunk));
            return handlers == null ? List.of() : new ArrayList<>(handlers);
        }

        private static void loadChunk(List<PersistentObjectHandler> persistentObjectHandlers) {
            for (PersistentObjectHandler persistentObjectHandler : persistentObjectHandlers)
                persistentObjectHandler.loadIfActive();
        }

        private static void unloadChunk(List<PersistentObjectHandler> persistentObjectHandlers) {
            for (PersistentObjectHandler persistentObjectHandler : persistentObjectHandlers)
                persistentObjectHandler.persistentObject.chunkUnload();
        }

        private static void unloadWorld(World world) {
            Set<PersistentObjectHandler> handlers = new HashSet<>();
            for (Map.Entry<ChunkKey, Set<PersistentObjectHandler>> entry : CHUNK_OBJECTS.entrySet()) {
                if (entry.getKey().worldId().equals(world.getUID())) handlers.addAll(entry.getValue());
            }
            for (PersistentObjectHandler persistentObjectHandler : new ArrayList<>(handlers))
                persistentObjectHandler.worldUnload();
        }

        private static void loadWorld(World world) {
            Set<PersistentObjectHandler> handlers = WORLD_OBJECTS.get(world.getName());
            if (handlers == null) return;
            for (PersistentObjectHandler persistentObjectHandler : new ArrayList<>(handlers))
                persistentObjectHandler.worldLoad(world);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void chunkLoadEvent(ChunkLoadEvent event) {
            List<PersistentObjectHandler> handlers = chunkHandlers(event.getChunk());
            if (handlers.isEmpty()) return;
            Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> loadChunk(handlers), 1L);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void worldUnloadEvent(WorldUnloadEvent event) {
            World unloadingWorld = event.getWorld();
            UUID worldId = unloadingWorld.getUID();
            // A MONITOR listener can still be followed by another MONITOR
            // registration that cancels the event. Observe the actual Bukkit
            // state next tick before moving every object to the world index.
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                if (Bukkit.getWorld(worldId) != null) return;
                unloadWorld(unloadingWorld);
            });
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void worldLoadEvent(WorldLoadEvent event) {
            loadWorld(event.getWorld());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void chunkUnloadEvent(ChunkUnloadEvent event) {
            UUID worldId = event.getWorld().getUID();
            int chunkX = event.getChunk().getX();
            int chunkZ = event.getChunk().getZ();
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                World liveWorld = Bukkit.getWorld(worldId);
                // A world unload owns the broader transition. If the world is
                // still live, only unload objects after this chunk really left.
                if (liveWorld == null || liveWorld.isChunkLoaded(chunkX, chunkZ)) return;
                Set<PersistentObjectHandler> indexed =
                        CHUNK_OBJECTS.get(new ChunkKey(worldId, chunkX, chunkZ));
                if (indexed != null && !indexed.isEmpty()) {
                    unloadChunk(new ArrayList<>(indexed));
                }
            });
        }
    }
}
