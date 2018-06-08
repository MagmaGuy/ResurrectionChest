package com.magmaguy.resurrectionchest;

import org.bstats.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ResurrectionChest extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        Metrics metrics = new Metrics(this);

        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.loadConfiguration();

        PlayerDataConfig playerDataConfig = new PlayerDataConfig();
        playerDataConfig.initializeConfig();

        ConfigValues.initializeConfigValues();

        this.getServer().getPluginManager().registerEvents(this, this);

        initializeParticleEffects();

    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {

        if (!event.getPlayer().hasPermission("resurrectionchest.use")) return;
        if (!event.getLine(0).equalsIgnoreCase("[DeathChest]") &&
                !event.getLine(1).equalsIgnoreCase("[DeathChest]")) return;
        if (event.getBlock().getType() != Material.WALL_SIGN) return;
        Sign sign = (Sign) event.getBlock().getState().getData();
        Block attached = event.getBlock().getRelative(sign.getAttachedFace());
        if (attached.getType() != Material.CHEST) return;

        String identifier = event.getPlayer().getUniqueId().toString();

        //set config value
        PlayerDataConfig playerDataConfig = new PlayerDataConfig();
        ConfigValues.playerDataConfig.set(identifier, attached.getLocation().toString());
        playerDataConfig.configuration.set(identifier, attached.getLocation().toString());
        playerDataConfig.customConfigLoader.saveCustomConfig(PlayerDataConfig.CONFIG_NAME);
        playerDataConfig.customConfigLoader.saveDefaultCustomConfig(PlayerDataConfig.CONFIG_NAME);

        ConfigValues.initializeConfigValues();

        event.setLine(0, "");
        event.setLine(1, ChatColor.translateAlternateColorCodes('&', "&5[DeathChest]"));
        event.setLine(2, event.getPlayer().getDisplayName());
        event.setLine(3, "");

        Bukkit.getLogger().info("[ResurrectionChest] Creating new ResurrectionChest for " + event.getPlayer().getName());

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if (!event.getEntity().hasPermission("resurrectionchest.use")) return;
        if (event.getKeepInventory()) return;
        if (!ConfigValues.playerDataConfig.contains(event.getEntity().getUniqueId().toString())) return;

        Player player = event.getEntity();

        if (player.getInventory().contains(Material.TOTEM)) return;

        Inventory newInventory = player.getInventory();
        newInventory.clear();

        for (ItemStack itemStack : event.getDrops()) {

            newInventory.addItem(itemStack);

        }

        event.getDrops().clear();

        Location blockLocation = locationParser(ConfigValues.playerDataConfig.getString(player.getUniqueId().toString()));
        if (blockLocation == null) return;

        Block deathChestBlock = blockLocation.getBlock();

        if (!deathChestBlock.getType().equals(Material.CHEST)) {

            player.sendMessage("Your DeathChest is no longer setup!");
            unregisterDeathChestEntry(blockLocation);
            return;

        }

        Chest deathChest = (Chest) deathChestBlock.getState();

        for (ItemStack itemStack : newInventory) {

            if (itemStack != null) {

                deathChest.getBlockInventory().addItem(itemStack);

            }

        }

        player.sendMessage("Your items have been moved to your DeathChest!");

    }

    private static Location locationParser(String locationString) {

        double x = 0;
        double y = 0;
        double z = 0;
        World world = null;

        if (locationString.split(",").length < 4) return null;

        for (String string : locationString.split(",")) {

            if (string.contains("name=")) {

                for (World worldName : Bukkit.getServer().getWorlds()) {

                    if (worldName.getName().equals(string.substring(string.lastIndexOf("=") + 1, string.indexOf("}")))) {

                        world = worldName;

                    }

                }

            }

            if (string.contains("x=")) {

                x = Double.parseDouble(string.substring(string.lastIndexOf("x=") + 2));

            }

            if (string.contains("y=")) {

                y = Double.parseDouble(string.substring(string.lastIndexOf("y=") + 2));

            }

            if (string.contains("z=")) {

                z = Double.parseDouble(string.substring(string.lastIndexOf("z=") + 2));

            }

        }

        return new Location(world, x, y, z);

    }

    @EventHandler
    public void onDeathChestBreak(BlockBreakEvent event) {

        if (event.isCancelled()) return;
        if (event.getBlock().getType().equals(Material.WALL_SIGN)) {

            if (((org.bukkit.block.Sign) event.getBlock().getState()).getLine(1).equals(ChatColor.translateAlternateColorCodes('&', "&5[DeathChest]"))) {

                Sign sign = (Sign) event.getBlock().getState().getData();
                Block attached = event.getBlock().getRelative(sign.getAttachedFace());

                Location attachedLocation = attached.getLocation();

                unregisterDeathChestEntry(attachedLocation);

                return;

            }

        }

        if (event.getBlock().getType().equals(Material.CHEST)) {

            unregisterDeathChestEntry(event.getBlock().getLocation());

        }

    }

    private void unregisterDeathChestEntry(Location location) {

        if (ConfigValues.playerDataConfig.getValues(true).containsValue(location.toString())) {

            for (String string : ConfigValues.playerDataConfig.getValues(true).keySet()) {

                if (ConfigValues.playerDataConfig.get(string).equals(location.toString())) {

                    PlayerDataConfig playerDataConfig = new PlayerDataConfig();
                    ConfigValues.playerDataConfig.set(string, null);
                    playerDataConfig.configuration.set(string, null);
                    playerDataConfig.customConfigLoader.saveCustomConfig(PlayerDataConfig.CONFIG_NAME);
                    playerDataConfig.customConfigLoader.saveDefaultCustomConfig(PlayerDataConfig.CONFIG_NAME);

                    ConfigValues.initializeConfigValues();

                }

            }

        }

    }

    private void initializeParticleEffects() {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (String string : ConfigValues.playerDataConfig.getKeys(true)) {

                    Location location = locationParser(ConfigValues.playerDataConfig.getString(string));

                    try {

                        if (location.getChunk().isLoaded()) {

                            location.add(new Vector(0.5, 0.5, 0.5));

                            location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 2, 0.075, 0.075, 0.75, 0.8);
                            location.getWorld().spawnParticle(Particle.PORTAL, location, 1, 0.075, 0.075, 0.75, 0.8);

                        }


                    } catch (Exception ignored) {
                    }

                }

            }

        }.runTaskTimer(this, 1, 1);

    }

}
