package dev.jsemolik.hytaleportal.util;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.TargetUtil;

import javax.annotation.Nullable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for raycasting operations
 */
public class RaycastHelper {
    
    private static final double MAX_RAYCAST_DISTANCE = 50.0; // Max distance to look for portal placement
    
    /**
     * Raycast from player's eyes to find the target block position
     * @param playerRef The player reference
     * @param world The world to raycast in
     * @return The position of the target block, or null if no block found
     */
    @Nullable
    public static Vector3i getTargetBlockPosition(PlayerRef playerRef, World world) {
        try {
            // Get the entity reference from PlayerRef
            var entityRef = playerRef.getReference();
            if (entityRef == null) {
                return null;
            }
            
            // Get the store (ComponentAccessor) from the entity reference
            var store = entityRef.getStore();
            
            // Use an array to capture the result from the world thread
            final Vector3i[] result = new Vector3i[1];
            final CountDownLatch latch = new CountDownLatch(1);
            
            // Execute raycasting on the world thread (required by TargetUtil)
            world.execute(() -> {
                try {
                    result[0] = TargetUtil.getTargetBlock(
                        entityRef,
                        MAX_RAYCAST_DISTANCE,
                        store
                    );
                } catch (Exception e) {
                    result[0] = null;
                } finally {
                    latch.countDown();
                }
            });
            
            // Wait for completion (max 5 seconds)
            latch.await(5, TimeUnit.SECONDS);
            
            return result[0];
            
        } catch (Exception e) {
            dev.jsemolik.hytaleportal.HytalePortal.getPluginLogger().atInfo().log(
                "Error getting target block: %s",
                e.getMessage()
            );
            return null;
        }
    }
    
    /**
     * Calculate portal position and rotation from target block and player position
     * Places portal adjacent to the hit surface, facing the player
     */
    public static PortalPlacement calculatePlacement(Vector3i targetBlock, Vector3d playerPos) {
        if (targetBlock == null) {
            // Fallback: place in front of player
            Vector3d pos = new Vector3d(
                Math.floor(playerPos.x),
                Math.floor(playerPos.y),
                Math.floor(playerPos.z) + 5
            );
            return new PortalPlacement(pos, new Vector3f(0, 0, 0));
        }
        
        // Calculate direction from target to player
        double dx = playerPos.x - targetBlock.x;
        double dy = playerPos.y - targetBlock.y;
        double dz = playerPos.z - targetBlock.z;
        
        // Determine which face was hit based on which axis has the largest difference
        double absDx = Math.abs(dx);
        double absDz = Math.abs(dz);
        
        Vector3d portalPos;
        float yaw;
        
        if (absDx > absDz) {
            // Hit on X-axis face (east/west wall)
            if (dx > 0) {
                // Hit west face, place portal on east side
                portalPos = new Vector3d(targetBlock.x + 1, targetBlock.y, targetBlock.z);
                yaw = 180; // Face west (adjusted +90)
            } else {
                // Hit east face, place portal on west side  
                portalPos = new Vector3d(targetBlock.x - 1, targetBlock.y, targetBlock.z);
                yaw = 0; // Face east (adjusted +90)
            }
        } else {
            // Hit on Z-axis face (north/south wall)
            if (dz > 0) {
                // Hit north face, place portal on south side
                portalPos = new Vector3d(targetBlock.x, targetBlock.y, targetBlock.z + 1);
                yaw = 270; // Face north (adjusted +90)
            } else {
                // Hit south face, place portal on north side
                portalPos = new Vector3d(targetBlock.x, targetBlock.y, targetBlock.z - 1);
                yaw = 90; // Face south (adjusted +90)
            }
        }
        
        return new PortalPlacement(portalPos, new Vector3f(0, yaw, 0));
    }
    
    /**
     * Helper class to hold portal position and rotation
     */
    public static class PortalPlacement {
        public final Vector3d position;
        public final Vector3f rotation;
        
        public PortalPlacement(Vector3d position, Vector3f rotation) {
            this.position = position;
            this.rotation = rotation;
        }
    }
}
