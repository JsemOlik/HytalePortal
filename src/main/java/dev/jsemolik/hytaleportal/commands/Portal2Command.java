package dev.jsemolik.hytaleportal.commands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.jsemolik.hytaleportal.portal.Portal;
import dev.jsemolik.hytaleportal.portal.PortalManager;
import dev.jsemolik.hytaleportal.portal.PortalType;

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

            // Get player position and rotation
            Vector3d playerPos = playerRef.getTransform().getPosition();
            Vector3f playerRot = playerRef.getHeadRotation();

            // Calculate portal placement (5 blocks in front)
            double distance = 5.0;
            float yaw = playerRot.y;
            double yawRadians = Math.toRadians(yaw);

            double x = playerPos.x - Math.sin(yawRadians) * distance;
            double y = playerPos.y;
            double z = playerPos.z + Math.cos(yawRadians) * distance;

            Vector3d portalPosition = new Vector3d(Math.floor(x), Math.floor(y), Math.floor(z));

            // Get world name from player
            var world = universe.getWorld(playerRef.getWorldUuid());
            if (world == null) {
                ctx.sendMessage(Message.raw("Could not find world!").color("red"));
                return;
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

            // Send feedback
            ctx.sendMessage(Message.raw("Orange Portal created!").color("gold"));

        } catch (Exception e) {
            ctx.sendMessage(Message.raw("Error: " + e.getMessage()).color("red"));
            e.printStackTrace();
        }
    }
}
