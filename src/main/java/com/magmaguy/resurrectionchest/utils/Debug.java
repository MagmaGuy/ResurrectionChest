package com.magmaguy.resurrectionchest.utils;

import org.bukkit.Bukkit;

public class Debug {
    public static void message(String message) {
        Bukkit.getLogger().warning("[ResurrectionChest]" + message);
    }
}
