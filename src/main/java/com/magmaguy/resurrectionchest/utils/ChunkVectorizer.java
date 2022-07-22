package com.magmaguy.resurrectionchest.utils;

import org.bukkit.Chunk;

import java.util.Objects;
import java.util.UUID;
import java.util.Vector;

public class ChunkVectorizer {

    public static int hash(Chunk chunk) {
        return Objects.hash(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
    }

    //pseudo-chunks - prevent it form having to load the chunk
    public static int hash(int x, int z, UUID worldUUID) {
        return Objects.hash(x, z, worldUUID);
    }

    public static Vector hash(double x, double z) {
        Vector vector = new Vector(2);
        vector.addElement(x);
        vector.addElement(z);
        return vector;
    }

    public static boolean isSameChunk(Chunk chunk, int hashedChunk) {
        return hash(chunk) == hashedChunk;
    }

}