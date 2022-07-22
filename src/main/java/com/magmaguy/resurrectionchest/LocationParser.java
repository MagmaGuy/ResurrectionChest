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
            if (string.contains("name="))
                for (World worldName : Bukkit.getServer().getWorlds())
                    if (worldName.getName().equals(string.substring(string.lastIndexOf("=") + 1, string.indexOf("}"))))
                        world = worldName;
            if (string.contains("x="))
                x = Double.parseDouble(string.substring(string.lastIndexOf("x=") + 2));

            if (string.contains("y="))
                y = Double.parseDouble(string.substring(string.lastIndexOf("y=") + 2));

            if (string.contains("z="))
                z = Double.parseDouble(string.substring(string.lastIndexOf("z=") + 2));

        }

        return new Location(world, x, y, z);

    }

}
