package dev.jsemolik.hytaleportal.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;

/**
 * Command to give a player the Portal Gun item.
 * Usage: /portalgun
 *
 * NOTE: This is a placeholder command. The portal gun item is: hytale:items/tools/grappling_hook
 * Use /give @s hytale:items/tools/grappling_hook to get the portal gun item.
 */
public class PortalGunCommand extends CommandBase {

    private static final String PORTAL_GUN_ITEM_ID = "hytale:items/tools/grappling_hook";

    public PortalGunCommand() {
        super("portalgun", "Tells you how to get the Portal Gun");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("=== Portal Gun ===").color("aqua").bold(true));
        ctx.sendMessage(Message.raw("To get the Portal Gun, use this command:").color("yellow"));
        ctx.sendMessage(Message.raw("/give @s " + PORTAL_GUN_ITEM_ID).color("green"));
        ctx.sendMessage(Message.raw(""));
        ctx.sendMessage(Message.raw("Usage:").color("yellow"));
        ctx.sendMessage(Message.raw("  Left-click: Create Blue Portal").color("blue"));
        ctx.sendMessage(Message.raw("  Right-click: Create Orange Portal").color("gold"));
    }
}
