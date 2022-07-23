package com.magmaguy.resurrectionchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationParser {

    public static Location parseLocation(String locationString) {

        double x = 0;
        double y = 0;
        double z = 0;
        World world = null;

        if (locationString.split(",").length < 4) return null;

        for (String string : locationString.split(",")) {
            if (string.contains("x="))
                x = Double.parseDouble(string.split("=")[1]);

            if (string.contains("y="))
                y = Double.parseDouble(string.split("=")[1]);

            if (string.contains("z="))
                z = Double.parseDouble(string.split("=")[1]);

        }

        world = Bukkit.getWorld(getWorldString(locationString));

        return new Location(world, x, y, z);

    }

    public static String getWorldString(String configString) {
        if (configString == null) return null;
        for (String string : configString.split("\\{"))
            if (string.contains("name="))
                return string.split("}")[0].split("=")[1];
        return null;
    }

}
