package com.magmaguy.resurrectionchest.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;

import java.util.List;

public class ResurrectionChestCommand extends AdvancedCommand {
    public ResurrectionChestCommand() {
        super(List.of());
        addArgument("action", new ListStringCommandArgument(
                List.of("setup", "initialize", "downloadall", "updatecontent", "reload"),
                "<action>"));
        setPermission("resurrectionchest.*");
        setDescription("Shares basic info about ResurrectionChest and points to the setup flow.");
        setUsage("/resurrectionchest");
        setSenderType(SenderType.ANY);
    }

    @Override
    public void execute(CommandData commandData) {
        Logger.sendMessage(commandData.getCommandSender(), "ResurrectionChest stores a player's dropped items inside a death chest instead of scattering them on the ground.");
        Logger.sendMessage(commandData.getCommandSender(), "The plugin works with vanilla chests by default, and optional Nightbreak content can add custom chest models through FreeMinecraftModels.");
        Logger.sendMessage(commandData.getCommandSender(), "Use &2/resurrectionchest initialize &ffor the first-time setup flow, or &2/resurrectionchest setup &fto browse Nightbreak-managed chest packs.");
        Logger.sendMessage(commandData.getCommandSender(), "If your Nightbreak account is linked, you can install all available ResurrectionChest content with &2/resurrectionchest downloadall&f.");
    }
}
