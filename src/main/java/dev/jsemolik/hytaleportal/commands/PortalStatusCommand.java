package dev.jsemolik.hytaleportal.commands;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.jsemolik.hytaleportal.portal.Portal;
import dev.jsemolik.hytaleportal.portal.PortalManager;
import dev.jsemolik.hytaleportal.portal.PortalPair;

import javax.annotation.Nonnull;

/**
 * Command to check portal status and locations.
 * Usage: /portalstatus or /pstatus
 */
public class PortalStatusCommand extends CommandBase {

    public PortalStatusCommand() {
        super("portalstatus", "Check your portal status and locations");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        try {
            // Get the first player (single player scenario)
            var universe = Universe.get();
            var players = universe.getPlayers();

            if (players.isEmpty()) {
                ctx.sendMessage(Message.raw("No players found!").color("red"));
                return;
            }

            var playerRef = players.get(0);
            var playerUUID = playerRef.getUuid();

            // Get portal pair
            PortalPair portalPair = PortalManager.getInstance().getPortalPair(playerUUID);

            if (portalPair == null || !portalPair.hasAnyPortal()) {
                ctx.sendMessage(Message.raw("You have no active portals").color("yellow"));
                ctx.sendMessage(Message.raw("Use /p1 and /p2 to create portals").color("gray"));
                return;
            }

            // Show header
            ctx.sendMessage(Message.raw("=== Portal Status ===").color("white").bold(true));

            // Show blue portal info
            Portal bluePortal = portalPair.getBluePortal();
            if (bluePortal != null) {
                Vector3d pos = bluePortal.getPosition();
                Vector3d center = bluePortal.getCenterPosition();
                ctx.sendMessage(Message.raw("Blue Portal:").color("blue").bold(true));
                ctx.sendMessage(Message.raw(String.format("  Position: X=%.1f, Y=%.1f, Z=%.1f", pos.x, pos.y, pos.z)).color("aqua"));
                ctx.sendMessage(Message.raw(String.format("  Center: X=%.1f, Y=%.1f, Z=%.1f", center.x, center.y, center.z)).color("aqua"));
                ctx.sendMessage(Message.raw("  World: " + bluePortal.getWorldName()).color("aqua"));

                // Show distance from player
                Vector3d playerPos = playerRef.getTransform().getPosition();
                double distance = calculateDistance(playerPos, center);
                ctx.sendMessage(Message.raw(String.format("  Distance from you: %.2f blocks", distance)).color("aqua"));
            } else {
                ctx.sendMessage(Message.raw("Blue Portal: Not created").color("gray"));
            }

            ctx.sendMessage(Message.raw(""));

            // Show orange portal info
            Portal orangePortal = portalPair.getOrangePortal();
            if (orangePortal != null) {
                Vector3d pos = orangePortal.getPosition();
                Vector3d center = orangePortal.getCenterPosition();
                ctx.sendMessage(Message.raw("Orange Portal:").color("gold").bold(true));
                ctx.sendMessage(Message.raw(String.format("  Position: X=%.1f, Y=%.1f, Z=%.1f", pos.x, pos.y, pos.z)).color("yellow"));
                ctx.sendMessage(Message.raw(String.format("  Center: X=%.1f, Y=%.1f, Z=%.1f", center.x, center.y, center.z)).color("yellow"));
                ctx.sendMessage(Message.raw("  World: " + orangePortal.getWorldName()).color("yellow"));

                // Show distance from player
                Vector3d playerPos = playerRef.getTransform().getPosition();
                double distance = calculateDistance(playerPos, center);
                ctx.sendMessage(Message.raw(String.format("  Distance from you: %.2f blocks", distance)).color("yellow"));
            } else {
                ctx.sendMessage(Message.raw("Orange Portal: Not created").color("gray"));
            }

            // Show teleportation status
            ctx.sendMessage(Message.raw(""));
            if (portalPair.hasBothPortals()) {
                ctx.sendMessage(Message.raw("Teleportation: ACTIVE").color("green").bold(true));
                ctx.sendMessage(Message.raw("Walk within 1.5 blocks of either portal to teleport!").color("green"));
            } else {
                ctx.sendMessage(Message.raw("Teleportation: INACTIVE (need both portals)").color("red").bold(true));
            }

        } catch (Exception e) {
            ctx.sendMessage(Message.raw("Error: " + e.getMessage()).color("red"));
            e.printStackTrace();
        }
    }

    /**
     * Calculate distance between two positions
     */
    private double calculateDistance(Vector3d pos1, Vector3d pos2) {
        double dx = pos1.x - pos2.x;
        double dy = pos1.y - pos2.y;
        double dz = pos1.z - pos2.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
