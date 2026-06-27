package com.magmaguy.resurrectionchest.commands;

import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.command.CommandManager;
import com.magmaguy.resurrectionchest.MockResurrectionChestPlugin;
import com.magmaguy.resurrectionchest.PersistentObjectHandler;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResurrectionChestCommandRoutingTest {
    private ServerMock server;
    private MockResurrectionChestPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.loadWith(
                MockResurrectionChestPlugin.class,
                new ByteArrayInputStream("""
                        name: ResurrectionChestTest
                        version: 1.0.0
                        main: com.magmaguy.resurrectionchest.MockResurrectionChestPlugin
                        api-version: '1.21'
                        commands:
                          resurrectionchest:
                            description: Test ResurrectionChest command.
                        permissions:
                          resurrectionchest.*:
                            default: op
                        """.getBytes(StandardCharsets.UTF_8)));
        MagmaCore.createInstance(plugin);
        CommandManager commandManager = new CommandManager(plugin, "resurrectionchest");
        commandManager.registerCommand(new ResurrectionChestCommand());
        commandManager.registerCommand(new ClearCommand());
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
    }

    @AfterEach
    void tearDown() throws Exception {
        ResurrectionChestObject.getResurrectionChests().clear();
        PersistentObjectHandler.shutdown();
        MagmaCore.shutdown(plugin);
        setStaticField(MagmaCore.class, "instance", null);
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void clearCommandRequiresAdminPermissionAndRootInfoCommandIsRoutable() {
        PlayerMock player = server.addPlayer("Keeper");

        assertTrue(server.dispatchCommand(server.getConsoleSender(), "resurrectionchest"));
        assertFalse(server.dispatchCommand(player, "resurrectionchest clear"));
        assertTrue(player.nextMessage().contains("permission"));

        player.setOp(true);

        assertTrue(server.dispatchCommand(player, "resurrectionchest clear"));
        assertTrue(player.nextMessage().contains("Cleared 0 tracked ResurrectionChest blocks."));
    }

    private static void setStaticField(Class<?> type, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
