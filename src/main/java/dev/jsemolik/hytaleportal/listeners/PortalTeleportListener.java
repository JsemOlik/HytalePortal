package dev.jsemolik.hytaleportal.listeners;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.jsemolik.hytaleportal.HytalePortal;
import dev.jsemolik.hytaleportal.portal.Portal;
import dev.jsemolik.hytaleportal.portal.PortalManager;
import dev.jsemolik.hytaleportal.portal.PortalPair;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles portal teleportation logic.
 * Monitors player positions and teleports them when they enter a portal.
 */
public class PortalTeleportListener {

    private static final long TELEPORT_COOLDOWN_MS = 1000; // 1 second cooldown after teleport

    // Track last teleport time for each player to prevent bouncing
    private static final Map<UUID, Long> lastTeleportTime = new ConcurrentHashMap<>();

    private static ScheduledFuture<?> checkTask;

    /**
     * Start the portal teleportation checker
     */
    public static void start() {
        if (checkTask != null && !checkTask.isCancelled()) {
            return; // Already running
        }

        // Check for portal teleportations every 50ms (20 times per second)
        checkTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
            PortalTeleportListener::checkPortalTeleports,
            0,
            50,
            TimeUnit.MILLISECONDS
        );

        HytalePortal.getPluginLogger().atInfo().log("Portal teleport checker started");
    }

    /**
     * Stop the portal teleportation checker
     */
    public static void stop() {
        if (checkTask != null) {
            checkTask.cancel(false);
            checkTask = null;
            HytalePortal.getPluginLogger().atInfo().log("Portal teleport checker stopped");
        }
        lastTeleportTime.clear();
    }

    /**
     * Check all players for portal teleportations
     */
    private static void checkPortalTeleports() {
        try {
            Universe universe = Universe.get();

            // Iterate through all players
            for (var playerRef : universe.getPlayers()) {
                UUID playerUUID = playerRef.getUuid();

                // Check cooldown
                Long lastTeleport = lastTeleportTime.get(playerUUID);
                if (lastTeleport != null && (System.currentTimeMillis() - lastTeleport) < TELEPORT_COOLDOWN_MS) {
                    continue; // Still on cooldown
                }

                // Get player's portal pair
                PortalPair portalPair = PortalManager.getInstance().getPortalPair(playerUUID);
                if (portalPair == null || !portalPair.hasBothPortals()) {
                    continue; // No portals or only one portal
                }

                // Get player position
                Vector3d playerPos = playerRef.getTransform().getPosition();

                // Get world safely
                var world = universe.getWorld(playerRef.getWorldUuid());
                if (world == null) {
                    continue; // Skip if world not found
                }
                String worldName = world.getName();

                // Check if player is near blue portal
                Portal bluePortal = portalPair.getBluePortal();
                if (bluePortal != null && bluePortal.getWorldName().equals(worldName)) {
                    if (isPlayerInsidePortal(playerPos, bluePortal)) {
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Player {} entering blue portal",
                            playerRef.getUsername()
                        );
                        teleportPlayer(playerRef, portalPair.getOrangePortal());
                        continue;
                    }
                }

                // Check if player is near orange portal
                Portal orangePortal = portalPair.getOrangePortal();
                if (orangePortal != null && orangePortal.getWorldName().equals(worldName)) {
                    if (isPlayerInsidePortal(playerPos, orangePortal)) {
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Player {} entering orange portal",
                            playerRef.getUsername()
                        );
                        teleportPlayer(playerRef, bluePortal);
                    }
                }
            }
        } catch (Exception e) {
            HytalePortal.getPluginLogger().atInfo().log("[ERROR] Error checking portal teleports: " + e.getMessage());
        }
    }

    /**
     * Check if a player is inside/touching a portal
     */
    private static boolean isPlayerInsidePortal(Vector3d playerPos, Portal portal) {
        // Get all portal block positions
        Vector3i[] framePositions = portal.getFramePositions();
        
        // Check if player position overlaps with any portal block
        // Player hitbox is approximately 0.6 x 1.8 x 0.6 blocks
        for (Vector3i blockPos : framePositions) {
            // Check if player's bounding box intersects with this block
            // Allow some margin for easier entry
            double margin = 0.8; // Slightly larger than player width for easier detection
            
            if (playerPos.x >= blockPos.x - margin && playerPos.x <= blockPos.x + 1 + margin &&
                playerPos.y >= blockPos.y - margin && playerPos.y <= blockPos.y + 1 + margin &&
                playerPos.z >= blockPos.z - margin && playerPos.z <= blockPos.z + 1 + margin) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Calculate distance between two positions
     */
    private static double calculateDistance(Vector3d pos1, Vector3d pos2) {
        double dx = pos1.x - pos2.x;
        double dy = pos1.y - pos2.y;
        double dz = pos1.z - pos2.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Teleport a player through a portal
     */
    private static void teleportPlayer(com.hypixel.hytale.server.core.universe.PlayerRef playerRef, Portal destinationPortal) {
        if (destinationPortal == null) {
            return;
        }

        try {
            // Get destination world
            World destinationWorld = Universe.get().getWorld(destinationPortal.getWorldName());
            if (destinationWorld == null) {
                HytalePortal.getPluginLogger().atInfo().log("[WARN] Cannot teleport player " + playerRef.getUsername() + ": destination world not found");
                return;
            }

            // Calculate destination position (center of destination portal)
            Vector3d destinationPos = destinationPortal.getCenterPosition();

            // Teleport the player on the world thread
            destinationWorld.execute(() -> {
                try {
                    // Get entity reference and store
                    var entityRef = playerRef.getReference();
                    if (entityRef == null) {
                        HytalePortal.getPluginLogger().atInfo().log("[WARN] Cannot teleport player: entity ref is null");
                        return;
                    }
                    
                    var store = entityRef.getStore();
                    
                    // Get current rotation components to preserve them
                    var headRotation = store.getComponent(entityRef, 
                        com.hypixel.hytale.server.core.modules.entity.component.HeadRotation.getComponentType());
                    var transformComponent = store.getComponent(entityRef,
                        com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
                    
                    if (headRotation == null || transformComponent == null) {
                        HytalePortal.getPluginLogger().atInfo().log("[WARN] Cannot teleport player: missing components");
                        return;
                    }
                    
                    Vector3f currentHeadRot = headRotation.getRotation();
                    Vector3f currentBodyRot = transformComponent.getRotation();
                    
                    // Create teleport component using the proper API
                    var teleport = com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.createForPlayer(
                        destinationPos,
                        currentBodyRot
                    ).setHeadRotation(currentHeadRot);
                    
                    // Add the teleport component to trigger teleportation
                    store.addComponent(entityRef, 
                        com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType(),
                        teleport);

                    // Record teleport time for cooldown
                    lastTeleportTime.put(playerRef.getUuid(), System.currentTimeMillis());

                    // Send feedback to player
                    playerRef.sendMessage(
                        Message.raw("*Whoosh*")
                            .color("aqua")
                            .italic(true)
                    );

                    HytalePortal.getPluginLogger().atInfo().log("Player " + playerRef.getUsername() + " teleported through portal to " + destinationPos);
                } catch (Exception e) {
                    HytalePortal.getPluginLogger().atInfo().log("[ERROR] Error teleporting player " + playerRef.getUsername() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            HytalePortal.getPluginLogger().atInfo().log("[ERROR] Error in teleportPlayer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if the teleport checker is running
     */
    public static boolean isRunning() {
        return checkTask != null && !checkTask.isCancelled();
    }
}
