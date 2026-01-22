package dev.jsemolik.hytaleportal.listeners;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.jsemolik.hytaleportal.HytalePortal;
import dev.jsemolik.hytaleportal.portal.Portal;
import dev.jsemolik.hytaleportal.portal.PortalManager;
import dev.jsemolik.hytaleportal.portal.PortalType;

/**
 * Listens for player interactions to handle portal gun usage.
 * Left-click creates a blue portal, right-click creates an orange portal.
 */
public class PortalGunListener {

    private static final String PORTAL_GUN_ITEM_ID = "hytale:items/tools/grappling_hook";
    private static final double MAX_PORTAL_DISTANCE = 100.0; // Maximum distance to place portal

    /**
     * Register this listener with the event registry
     */
    public static void register(HytalePortal plugin) {
        // PlayerInteractEvent uses World as the key type
        plugin.getEventRegistry().registerGlobal(PlayerInteractEvent.class, event -> {
            handleInteraction(event);
        });
    }

    private static void handleInteraction(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();

        // Check if player is holding the portal gun
        if (itemInHand == null || itemInHand.isEmpty()) {
            return;
        }

        if (!PORTAL_GUN_ITEM_ID.equals(itemInHand.getItemId())) {
            return;
        }

        // Get actionType as string to check interaction type
        String actionTypeStr = event.getActionType().toString();

        // Determine portal type based on interaction
        PortalType portalType = null;
        if (actionTypeStr.contains("LEFT_CLICK")) {
            portalType = PortalType.BLUE;
        } else if (actionTypeStr.contains("RIGHT_CLICK")) {
            portalType = PortalType.ORANGE;
        }

        if (portalType == null) {
            return;
        }

        // Cancel the default interaction
        event.setCancelled(true);

        // Get player reference from Universe
        PlayerRef playerRef = Universe.get().getPlayer(player.getUuid());
        if (playerRef == null) {
            return;
        }

        // Get player's look direction and position
        World world = player.getWorld();
        Vector3d playerPos = playerRef.getTransform().getPosition();
        Vector3f playerRot = playerRef.getHeadRotation();

        // Calculate portal placement position
        // For now, place it directly in front of the player at a fixed distance
        // TODO: Implement proper raycast to find the nearest surface
        Vector3d portalPosition = calculatePortalPosition(playerPos, playerRot);

        // Create the portal
        Portal portal = new Portal(
            playerRef.getUuid(),
            portalType,
            portalPosition,
            new Vector3f(0, 0, 0), // Default rotation for now
            world.getName()
        );

        // Register the portal
        PortalManager.getInstance().setPortal(playerRef.getUuid(), portal);

        // Send feedback to player
        String colorName = portalType == PortalType.BLUE ? "Blue" : "Orange";
        String color = portalType == PortalType.BLUE ? "blue" : "gold";
        player.sendMessage(
            Message.raw(colorName + " portal created!")
                .color(color)
        );

        HytalePortal.getPluginLogger().atInfo().log("Player " + playerRef.getUsername() + " created a " + colorName + " portal at " + portalPosition);
    }

    /**
     * Calculate the portal placement position based on player position and rotation
     * TODO: Implement proper raycast to find the nearest block surface
     */
    private static Vector3d calculatePortalPosition(Vector3d playerPos, Vector3f playerRot) {
        // Simple calculation: place portal 5 blocks in front of player
        double distance = 5.0;

        // Convert rotation to direction vector
        // Yaw is rotation around Y axis
        float yaw = playerRot.y;
        double yawRadians = Math.toRadians(yaw);

        double x = playerPos.x - Math.sin(yawRadians) * distance;
        double y = playerPos.y; // Same height as player for now
        double z = playerPos.z + Math.cos(yawRadians) * distance;

        // Snap to block coordinates
        return new Vector3d(Math.floor(x), Math.floor(y), Math.floor(z));
    }
}
