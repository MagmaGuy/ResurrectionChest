package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationParser {

    private static final Pattern LOCATION_FIELD_PATTERN = Pattern.compile("(?:^|[,{}])\\s*(x|y|z|pitch|yaw)=([^,{}]+)");
    private static final Pattern CRAFT_WORLD_NAME_PATTERN = Pattern.compile("CraftWorld\\{name=([^}]+)}");
    private static final Pattern CRAFT_WORLD_KEY_PATTERN = Pattern.compile("CraftWorld\\{key=([^}]+)}");
    private static final Pattern WORLD_FIELD_PATTERN = Pattern.compile("(?:^|[,{}])\\s*world=([^,{}]+)");

    public static String serializeLocation(Location location) {
        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Cannot serialize a location without a world");

        return "world=" + location.getWorld().getName() +
                ",x=" + location.getX() +
                ",y=" + location.getY() +
                ",z=" + location.getZ() +
                ",yaw=" + location.getYaw() +
                ",pitch=" + location.getPitch();
    }

    public static Location parseLocation(String locationString) {
        if (locationString == null || locationString.isBlank())
            throw new IllegalArgumentException("Location string is empty");

        Map<String, String> fields = new HashMap<>();
        Matcher matcher = LOCATION_FIELD_PATTERN.matcher(locationString);
        while (matcher.find())
            fields.put(matcher.group(1), matcher.group(2).trim());

        double x = parseRequiredDouble(fields, "x");
        double y = parseRequiredDouble(fields, "y");
        double z = parseRequiredDouble(fields, "z");
        float yaw = parseOptionalFloat(fields, "yaw");
        float pitch = parseOptionalFloat(fields, "pitch");

        String worldName = getWorldString(locationString);
        World world = worldName == null ? null : Bukkit.getWorld(worldName);

        return new Location(world, x, y, z, yaw, pitch);

    }

    private static double parseRequiredDouble(Map<String, String> fields, String key) {
        if (!fields.containsKey(key))
            throw new IllegalArgumentException("Location string is missing " + key);
        return parseDouble(fields.get(key), key);
    }

    private static float parseOptionalFloat(Map<String, String> fields, String key) {
        if (!fields.containsKey(key)) return 0;
        return (float) parseDouble(fields.get(key), key);
    }

    private static double parseDouble(String value, String key) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + key + " value in location string: " + value, e);
        }
    }

    public static String getWorldString(String configString) {
        if (configString == null) return null;

        String worldString = getLegacyCraftWorldString(configString);
        if (worldString == null)
            worldString = getSerializedWorldString(configString);
        if (worldString == null) return null;

        World world = getWorldByNameOrKey(worldString);
        if (world != null) return world.getName();

        return normalizeWorldKey(worldString);
    }

    private static String getLegacyCraftWorldString(String configString) {
        Matcher nameMatcher = CRAFT_WORLD_NAME_PATTERN.matcher(configString);
        if (nameMatcher.find()) return nameMatcher.group(1);

        Matcher keyMatcher = CRAFT_WORLD_KEY_PATTERN.matcher(configString);
        if (keyMatcher.find()) return keyMatcher.group(1);

        return null;
    }

    private static String getSerializedWorldString(String configString) {
        Matcher matcher = WORLD_FIELD_PATTERN.matcher(configString);
        if (!matcher.find()) return null;

        String worldString = matcher.group(1).trim();
        if (worldString.startsWith("CraftWorld")) return null;
        return worldString;
    }

    private static World getWorldByNameOrKey(String worldString) {
        World world = Bukkit.getWorld(worldString);
        if (world != null) return world;

        for (World candidate : Bukkit.getWorlds())
            if (candidate.getKey().toString().equals(worldString))
                return candidate;

        String normalizedWorldString = normalizeWorldKey(worldString);
        if (!normalizedWorldString.equals(worldString))
            return Bukkit.getWorld(normalizedWorldString);

        return null;
    }

    private static String normalizeWorldKey(String worldString) {
        int namespaceSeparator = worldString.indexOf(':');
        if (namespaceSeparator == -1 || namespaceSeparator + 1 >= worldString.length())
            return worldString;
        return worldString.substring(namespaceSeparator + 1);
    }

}
