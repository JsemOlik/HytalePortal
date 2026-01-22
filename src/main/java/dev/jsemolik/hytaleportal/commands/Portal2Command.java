package dev.jsemolik.hytaleportal.commands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.jsemolik.hytaleportal.portal.Portal;
import dev.jsemolik.hytaleportal.portal.PortalManager;
import dev.jsemolik.hytaleportal.portal.PortalType;
import dev.jsemolik.hytaleportal.util.RaycastHelper;

import javax.annotation.Nonnull;

/**
 * Command to create an orange portal.
 * Usage: /p2
 */
public class Portal2Command extends CommandBase {

    public Portal2Command() {
        super("p2", "Creates an Orange Portal in front of you");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        ctx.sendMessage(Message.raw("Creating Orange Portal...").color("gold"));

        try {
            // Get all players - use first one as test (single player scenario)
            var universe = Universe.get();
            var players = universe.getPlayers();

            if (players.isEmpty()) {
                ctx.sendMessage(Message.raw("No players found!").color("red"));
                return;
            }

            var playerRef = players.get(0);

            // Get player position
            Vector3d playerPos = playerRef.getTransform().getPosition();

            // Get world name from player
            var world = universe.getWorld(playerRef.getWorldUuid());
            if (world == null) {
                ctx.sendMessage(Message.raw("Could not find world!").color("red"));
                return;
            }

            // Raycast to find target block
            Vector3i targetBlock = RaycastHelper.getTargetBlockPosition(playerRef, world);
            Vector3d portalPosition = RaycastHelper.calculatePortalPosition(targetBlock, playerPos);

            if (targetBlock == null) {
                ctx.sendMessage(Message.raw("No surface found! Placing at default location.").color("yellow"));
            }

            // Create the orange portal
            Portal portal = new Portal(
                playerRef.getUuid(),
                PortalType.ORANGE,
                portalPosition,
                new Vector3f(0, 0, 0),
                world.getName()
            );

            // Register the portal
            PortalManager.getInstance().setPortal(playerRef.getUuid(), portal);

            // Debug logging
            dev.jsemolik.hytaleportal.HytalePortal.getPluginLogger().atInfo().log(
                "Portal2Command: Registered orange portal for player {} at position ({}, {}, {})",
                playerRef.getUuid(), portalPosition.x, portalPosition.y, portalPosition.z
            );

            // Send detailed feedback with location
            ctx.sendMessage(Message.raw("Orange Portal created!").color("gold"));
            ctx.sendMessage(Message.raw(String.format("Location: X=%.1f, Y=%.1f, Z=%.1f",
                portalPosition.x, portalPosition.y, portalPosition.z)).color("gold"));
            ctx.sendMessage(Message.raw("Walk to this location to test teleportation").color("gray").italic(true));

        } catch (Exception e) {
            ctx.sendMessage(Message.raw("Error: " + e.getMessage()).color("red"));
            e.printStackTrace();
        }
    }
}
