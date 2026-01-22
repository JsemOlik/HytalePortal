package dev.jsemolik.hytaleportal.util;

import com.hypixel.hytale.math.vector.Vector3d;
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
     * Calculate portal position from target block
     * Places portal adjacent to the hit surface
     */
    public static Vector3d calculatePortalPosition(Vector3i targetBlock, Vector3d playerPos) {
        if (targetBlock == null) {
            // Fallback: place in front of player based on position
            return new Vector3d(
                Math.floor(playerPos.x),
                Math.floor(playerPos.y),
                Math.floor(playerPos.z) + 5
            );
        }
        
        // Place portal at the target block position
        // The portal will be on the surface the player is looking at
        return new Vector3d(
            targetBlock.x,
            targetBlock.y,
            targetBlock.z
        );
    }
}
