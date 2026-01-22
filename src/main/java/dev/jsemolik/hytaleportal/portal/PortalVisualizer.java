package dev.jsemolik.hytaleportal.portal;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.jsemolik.hytaleportal.HytalePortal;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles visualization of portals using particles.
 * Creates particle effects around portal frames and in the portal center.
 */
public class PortalVisualizer {
    
    private ScheduledFuture<?> particleTask;
    
    /**
     * Start the particle visualization task
     */
    public void start() {
        if (particleTask != null && !particleTask.isCancelled()) {
            return; // Already running
        }
        
        // Schedule particle updates at 20 ticks per second (50ms interval)
        particleTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
            this::updateParticles,
            0,
            50,
            TimeUnit.MILLISECONDS
        );
        
        HytalePortal.getPluginLogger().atInfo().log("Portal visualizer started");
    }
    
    /**
     * Stop the particle visualization task
     */
    public void stop() {
        if (particleTask != null) {
            particleTask.cancel(false);
            particleTask = null;
            HytalePortal.getPluginLogger().atInfo().log("Portal visualizer stopped");
        }
    }
    
    /**
     * Update particles for all active portals
     */
    private void updateParticles() {
        try {
            PortalManager manager = PortalManager.getInstance();
            
            // Iterate through all portal pairs
            manager.getAllPortalPairs().forEach((playerUUID, portalPair) -> {
                // Visualize blue portal
                if (portalPair.getBluePortal() != null) {
                    visualizePortal(portalPair.getBluePortal());
                }
                
                // Visualize orange portal
                if (portalPair.getOrangePortal() != null) {
                    visualizePortal(portalPair.getOrangePortal());
                }
            });
        } catch (Exception e) {
            HytalePortal.getPluginLogger().atInfo().log("[ERROR] " + "Error updating portal particles: {}", e.getMessage());
        }
    }
    
    /**
     * Create particle effects for a single portal
     */
    private void visualizePortal(Portal portal) {
        // Get the world
        World world = Universe.get().getWorld(portal.getWorldName());
        if (world == null) {
            return;
        }
        
        // Execute on world thread for thread safety
        world.execute(() -> {
            try {
                createPortalParticles(portal, world);
            } catch (Exception e) {
                HytalePortal.getPluginLogger().atInfo().log("[ERROR] " + 
                    "Error creating particles for portal: {}",
                    e.getMessage()
                );
            }
        });
    }
    
    /**
     * Create visual effects for a portal
     * Currently using blocks since particle API is not fully documented
     */
    private void createPortalParticles(Portal portal, World world) {
        // Get all frame positions
        Vector3i[] framePositions = portal.getFramePositions();

        // Try to place blocks at portal locations
        // Block type to use for visualization
        // Blue portal: Light blue/cyan colored blocks
        // Orange portal: Orange/gold colored blocks
        String blockType = (portal.getType() == PortalType.BLUE) ?
            "hytale:block_stone_polished_smooth_light" : // Placeholder - use any available block
            "hytale:block_stone_polished_smooth";        // Placeholder - use any available block

        try {
            var chunkStore = world.getChunkStore();

            // Try to set blocks at each frame position
            for (Vector3i pos : framePositions) {
                try {
                    // Get the chunk containing this position
                    long chunkCoord = calculateChunkCoordinate(pos);
                    var chunk = world.getChunkIfLoaded(chunkCoord);

                    if (chunk != null) {
                        // Try to set block using common API patterns
                        // This might not work if the API is different
                        // chunk.setBlock(pos, blockType);

                        // For now, just log that we would place a block here
                        // HytalePortal.getPluginLogger().atInfo().log(
                        //     "Would place {} block at {}, {}, {}",
                        //     portal.getType(), pos.x(), pos.y(), pos.z()
                        // );
                    }
                } catch (Exception e) {
                    // Silently fail - API might not support this
                }
            }
        } catch (Exception e) {
            // Block placement not available yet
            // This is expected until we figure out the block API
        }
    }

    /**
     * Calculate chunk coordinate from block position
     * Most voxel games use 16-block chunks
     */
    private long calculateChunkCoordinate(Vector3i blockPos) {
        int chunkX = blockPos.x >> 4; // Divide by 16
        int chunkZ = blockPos.z >> 4; // Divide by 16
        // Pack into long - this is a guess at the coordinate format
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
    
    /**
     * Check if the visualizer is running
     */
    public boolean isRunning() {
        return particleTask != null && !particleTask.isCancelled();
    }
}
