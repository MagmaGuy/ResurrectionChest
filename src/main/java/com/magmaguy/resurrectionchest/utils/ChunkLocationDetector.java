package com.magmaguy.resurrectionchest.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkLocationDetector {

    public static boolean chunkLocationCheck(Location location, Chunk chunk) {
        if (!chunk.getWorld().equals(location.getWorld())) return false;
        double chunkX = chunk.getX() * 16D;
        double locationX = location.getX();
        double chunkZ = chunk.getZ() * 16D;
        double locationZ = location.getZ();
        if (!(chunkX <= locationX && chunkX + 16 >= locationX))
            return false;
        return chunkZ <= locationZ && chunkZ + 16 >= locationZ;
    }

}
