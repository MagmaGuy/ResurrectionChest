package com.magmaguy.resurrectionchest;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.resurrectionchest.utils.ChunkVectorizer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ChunkEntity {

    private static final HashSet<Integer> loadingChunks = new HashSet<>();

    private Location location;
    private int chunk;
    private String worldName;
    public ResurrectionChestObject resurrectionChestObject;

    private static ArrayListMultimap<Integer, ChunkEntity> chunkEntities = ArrayListMultimap.create();
    public static ArrayListMultimap<Integer, ChunkEntity> getChunkEntities(){
        return chunkEntities;
    }

    public ChunkEntity(Location location, ResurrectionChestObject resurrectionChestObject) {
        this.location = location;
        this.worldName = location.getWorld().getName();
        this.resurrectionChestObject = resurrectionChestObject;
        this.chunk = ChunkVectorizer.hash(
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                location.getWorld().getUID());
        chunkEntities.put(chunk, this);
        worldEntities.put(worldName, this);
    }

    private static ArrayListMultimap<String, ChunkEntity> worldEntities = ArrayListMultimap.create();
    public static ArrayListMultimap<String, ChunkEntity> getWorldEntities(){
        return worldEntities;
    }

    public static void loadChunk(List<ChunkEntity> loadedChunkEntities) {
        loadedChunkEntities.forEach(thisEntity -> thisEntity.resurrectionChestObject.load());
    }

    public static void unloadChunk(List<ChunkEntity> loadedChunkEntities) {
        loadedChunkEntities.forEach(thisEntity -> thisEntity.resurrectionChestObject.unload(false));
    }

    public static void loadWorld(List<ChunkEntity> loadedChunkEntities, World world) {
        loadedChunkEntities.forEach(thisEntity -> {
            thisEntity.location = new Location(world, thisEntity.location.getX(), thisEntity.location.getY(), thisEntity.location.getZ());
            thisEntity.chunk = ChunkVectorizer.hash(
                    thisEntity.location.getBlockX() >> 4,
                    thisEntity.location.getBlockZ() >> 4,
                    world.getUID());
            chunkEntities.put(thisEntity.chunk, thisEntity);
            thisEntity.resurrectionChestObject.load(world);
        });

    }

    public static void unloadWorld(ChunkEntity loadedChunkEntity) {
        loadedChunkEntity.resurrectionChestObject.unload(true);
    }

    public static class ChunkEntityEvents implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onChunkLoad(ChunkLoadEvent event) {
            int chunkLocation = ChunkVectorizer.hash(event.getChunk());
            List<ChunkEntity> simplePersistentEntityList = new ArrayList<>(chunkEntities.get(chunkLocation));
            if (loadingChunks.contains(chunkLocation)) return;
            loadingChunks.add(chunkLocation);
            loadChunk(simplePersistentEntityList);
            loadingChunks.remove(chunkLocation);
        }

        @EventHandler(ignoreCancelled = true)
        public void onChunkUnload(ChunkUnloadEvent event) {
            int chunkLocation = ChunkVectorizer.hash(event.getChunk());
            List<ChunkEntity> simplePersistentEntityList = new ArrayList<>(chunkEntities.get(chunkLocation));
            if (loadingChunks.contains(chunkLocation)) return;
            loadingChunks.add(chunkLocation);
            unloadChunk(simplePersistentEntityList);
            loadingChunks.remove(chunkLocation);
        }

        @EventHandler(ignoreCancelled = true)
        public void onWorldLoad(WorldLoadEvent event) {
            List<ChunkEntity> simplePersistentEntityList = new ArrayList<>(worldEntities.get(event.getWorld().getName()));
            loadWorld(simplePersistentEntityList, event.getWorld());
        }

        @EventHandler(ignoreCancelled = true)
        public void onWorldUnload(WorldUnloadEvent event) {
            for (ChunkEntity chunkEntity : chunkEntities.values())
                if (chunkEntity.worldName.equals(event.getWorld().getName()))
                    unloadWorld(chunkEntity);
        }
    }
}
