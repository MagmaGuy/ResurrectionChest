package com.magmaguy.resurrectionchest.commands;

import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.resurrectionchest.ResurrectionChestObject;

import java.util.List;

public class ClearCommand extends AdvancedCommand {
    public ClearCommand() {
        super(List.of("clear"));
        setDescription("Clears all tracked resurrection chest blocks and data.");
        setPermission("resurrectionchest.*");
        setUsage("/resurrectionchest clear");
    }

    @Override
    public void execute(CommandData commandData) {
        int removedBlocks = ResurrectionChestObject.clearAllTrackedBlocks();
        Logger.sendMessage(commandData.getCommandSender(), "&aCleared " + removedBlocks + " tracked ResurrectionChest blocks.");
    }
}
