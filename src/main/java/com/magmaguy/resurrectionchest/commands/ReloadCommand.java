package com.magmaguy.resurrectionchest.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginBootstrap;
import com.magmaguy.resurrectionchest.MetadataHandler;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends AdvancedCommand {
    public ReloadCommand() {
        super(List.of("reload"));
        setDescription("Reloads the plugin.");
        setPermission("resurrectionchest.*");
        setUsage("/resurrectionchest reload");
    }

    public static void reload(CommandSender sender) {
        NightbreakPluginBootstrap.setPendingReloadSender((org.bukkit.plugin.java.JavaPlugin) MetadataHandler.PLUGIN, sender);
        MetadataHandler.PLUGIN.onDisable();
        MetadataHandler.PLUGIN.onLoad();
        MetadataHandler.PLUGIN.onEnable();
    }

    @Override
    public void execute(CommandData commandData) {
        reload(commandData.getCommandSender());
    }
}
