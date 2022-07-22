package com.magmaguy.resurrectionchest.utils;

import org.bukkit.Location;

public class ChunkLoadCheck {
    /*
    Checks if a given location is loaded
    */
    public static boolean locationIsLoaded(Location location) {
        return location.getWorld() != null &&
                location.getWorld().isChunkLoaded(location.getBlock().getX() >> 4, location.getBlock().getZ() >> 4);
    }
}
