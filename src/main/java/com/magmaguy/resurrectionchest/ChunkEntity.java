package com.magmaguy.resurrectionchest;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.resurrectionchest.utils.ChunkVectorizer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ChunkEntity {

    private static final HashSet<Integer> loadingChunks = new HashSet<>();

    private Location location;
    private int chunk;
    private String worldName;
    private ResurrectionChestObject resurrectionChestObject;

    private static ArrayListMultimap<Integer, ChunkEntity> chunkEntities = ArrayListMultimap.create();

    public ChunkEntity(Location location, ResurrectionChestObject resurrectionChestObject) {
        this.location = location;
        this.worldName = location.getWorld().getName();
        this.resurrectionChestObject = resurrectionChestObject;
        this.chunk = ChunkVectorizer.hash(
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                location.getWorld().getUID());
        chunkEntities.put(chunk, this);
    }

    public static void loadChunk(List<ChunkEntity> loadedChunkEntities) {
        loadedChunkEntities.forEach(thisEntity -> thisEntity.resurrectionChestObject.load());
    }

    public static void unloadChunk(List<ChunkEntity> loadedChunkEntities) {
        loadedChunkEntities.forEach(thisEntity -> thisEntity.resurrectionChestObject.unload());
    }

    public static class ChunkEntityEvents implements Listener {
        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            int chunkLocation = ChunkVectorizer.hash(event.getChunk());
            List<ChunkEntity> simplePersistentEntityList = new ArrayList<>(chunkEntities.get(chunkLocation));
            if (loadingChunks.contains(chunkLocation)) return;
            loadingChunks.add(chunkLocation);
            loadChunk(simplePersistentEntityList);
            loadingChunks.remove(chunkLocation);
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            int chunkLocation = ChunkVectorizer.hash(event.getChunk());
            List<ChunkEntity> simplePersistentEntityList = new ArrayList<>(chunkEntities.get(chunkLocation));
            if (loadingChunks.contains(chunkLocation)) return;
            loadingChunks.add(chunkLocation);
            unloadChunk(simplePersistentEntityList);
            loadingChunks.remove(chunkLocation);
        }

        @EventHandler
        public void onWorldLoad() {

        }

        @EventHandler
        public void onWorldUnload() {

        }
    }
}
