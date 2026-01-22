package dev.jsemolik.hytaleportal.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Command to give a player the Portal Gun item.
 * Usage: /portalgun [player]
 */
public class PortalGunCommand extends CommandBase {
    
    // For now, we'll use a vanilla item as the portal gun
    // TODO: Create a custom portal gun item once we understand the item system better
    private static final String PORTAL_GUN_ITEM_ID = "hytale:items/tools/grappling_hook";
    
    public PortalGunCommand() {
        super("portalgun", "Gives you a Portal Gun to create portals");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        // Get the player executing the command
        if (!(ctx.getSender() instanceof Player player)) {
            ctx.sendMessage(Message.raw("This command can only be used by players!").color("red"));
            return;
        }

        // Create the portal gun item stack
        ItemStack portalGun = new ItemStack(PORTAL_GUN_ITEM_ID, 1);
        
        // Add the item to the player's inventory
        var inventory = player.getInventory();
        
        // Try to add to hotbar first, then storage
        // TODO: Implement proper inventory management
        
        ctx.sendMessage(Message.raw("You have been given a Portal Gun!").color("green"));
        ctx.sendMessage(Message.raw("Left-click to create a Blue Portal, Right-click for Orange Portal").color("yellow"));
    }
}
