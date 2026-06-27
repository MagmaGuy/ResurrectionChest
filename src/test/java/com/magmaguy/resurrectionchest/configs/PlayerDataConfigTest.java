package com.magmaguy.resurrectionchest.configs;

import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.resurrectionchest.LocationParser;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerDataConfigTest {
    private ServerMock server;
    private JavaPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("ResurrectionChest");
        MagmaCore.createInstance(plugin);
        server.addSimpleWorld("playerdata_world");
        new PlayerDataConfig();
    }

    @AfterEach
    void tearDown() throws Exception {
        MagmaCore.shutdown(plugin);
        setStaticField(MagmaCore.class, "instance", null);
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void playerDataCanBeAddedReadAndRemovedByUuid() {
        UUID uuid = UUID.randomUUID();
        World world = server.getWorld("playerdata_world");
        Location location = new Location(world, 12.5, 65, -3.5, 90, 15);

        PlayerDataConfig.addPlayerdata(uuid, location, "resurrectionchest_free");

        String locationString = PlayerDataConfig.getInstance().getFileConfiguration().getString(uuid + ".location");
        assertEquals(LocationParser.serializeLocation(location), locationString);
        assertEquals("resurrectionchest_free", PlayerDataConfig.getInstance().getFileConfiguration().getString(uuid + ".chestModel"));

        PlayerDataConfig.removePlayerData(uuid);

        assertEquals(null, PlayerDataConfig.getInstance().getFileConfiguration().getString(uuid + ".location"));
    }

    private static void setStaticField(Class<?> type, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
