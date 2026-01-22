package dev.jsemolik.hytaleportal.portal;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.jsemolik.hytaleportal.HytalePortal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles visualization of portals using particles.
 * Creates particle effects around portal frames and in the portal center.
 */
public class PortalVisualizer {
    
    private ScheduledFuture<?> particleTask;
    
    // Track which portals have had their blocks placed (using creation time as unique ID)
    private final Set<Long> placedPortals = ConcurrentHashMap.newKeySet();
    
    /**
     * Start the particle visualization task
     */
    public void start() {
        if (particleTask != null && !particleTask.isCancelled()) {
            HytalePortal.getPluginLogger().atInfo().log("Portal visualizer already running");
            return; // Already running
        }

        HytalePortal.getPluginLogger().atInfo().log("Starting portal visualizer...");

        // Schedule particle updates at 20 ticks per second (50ms interval)
        particleTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
            this::updateParticles,
            0,
            50,
            TimeUnit.MILLISECONDS
        );

        HytalePortal.getPluginLogger().atInfo().log("Portal visualizer started successfully");
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
        // Clear tracking set
        placedPortals.clear();
    }
    
    /**
     * Update particles for all active portals
     */
    private void updateParticles() {
        try {
            PortalManager manager = PortalManager.getInstance();
            var portalPairs = manager.getAllPortalPairs();

            // Iterate through all portal pairs
            portalPairs.forEach((playerUUID, portalPair) -> {
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
            e.printStackTrace();
        }
    }
    
    /**
     * Create particle effects for a single portal
     */
    private void visualizePortal(Portal portal) {
        // Only place blocks for portals that haven't been placed yet
        long portalId = portal.getCreationTime();
        
        if (placedPortals.contains(portalId)) {
            // Blocks already placed for this portal - just render particles in future
            return;
        }
        
        // Get the world
        World world = Universe.get().getWorld(portal.getWorldName());
        if (world == null) {
            return;
        }
        
        // Execute on world thread for thread safety
        world.execute(() -> {
            try {
                placePortalBlocks(portal, world);
                // Mark this portal as having its blocks placed
                placedPortals.add(portalId);
            } catch (Exception e) {
                HytalePortal.getPluginLogger().atInfo().log("[ERROR] " + 
                    "Error creating blocks for portal: {}",
                    e.getMessage()
                );
            }
        });
    }
    
    /**
     * Place blocks for a portal (called once per portal)
     */
    private void placePortalBlocks(Portal portal, World world) {
        // Get all frame positions
        Vector3i[] framePositions = portal.getFramePositions();

        // Block type to use for visualization
        // Blue portal: Blue crystal blocks
        // Orange portal: Red crystal blocks
        String blockType = (portal.getType() == PortalType.BLUE) ?
            "Cloth_Block_Wool_Blue" :  // Blue portal
            "Cloth_Block_Wool_Orange";     // Orange portal

        try {
            // Log portal creation attempt
            HytalePortal.getPluginLogger().atInfo().log(
                "Creating %s portal with %s blocks at %d positions",
                portal.getType(), blockType, framePositions.length
            );

            // Place blocks at each frame position
            for (Vector3i pos : framePositions) {
                try {
                    // Get the chunk containing this position
                    long chunkCoord = calculateChunkCoordinate(pos);
                    var chunk = world.getChunkIfLoaded(chunkCoord);

                    if (chunk == null) {
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Chunk not loaded for portal block at %d, %d, %d",
                            pos.x, pos.y, pos.z
                        );
                        continue;
                    }

                    // BlockAccessor.setBlock() expects chunk-relative coordinates (0-31)
                    // but pos contains world coordinates, so we need to convert
                    // Chunks are 32x32x32 blocks
                    int chunkRelativeX = pos.x & 0x1F;  // pos.x % 32
                    int chunkRelativeY = pos.y & 0x1F;  // pos.y % 32
                    int chunkRelativeZ = pos.z & 0x1F;  // pos.z % 32

                    // Use the BlockAccessor interface to place blocks
                    // Use Debug_Block for testing - it can be placed anywhere
                    boolean placed = chunk.setBlock(chunkRelativeX, chunkRelativeY, chunkRelativeZ, "Debug_Block");

                    if (!placed) {
                        // Log if placement failed
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Failed to place %s portal block at %d, %d, %d (chunk-rel: %d, %d, %d) - setBlock returned false",
                            portal.getType(), pos.x, pos.y, pos.z, chunkRelativeX, chunkRelativeY, chunkRelativeZ
                        );
                    } else {
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Successfully placed %s portal block at %d, %d, %d (chunk-rel: %d, %d, %d)",
                            portal.getType(), pos.x, pos.y, pos.z, chunkRelativeX, chunkRelativeY, chunkRelativeZ
                        );
                    }
                } catch (Exception e) {
                    // Log any exceptions for debugging
                    HytalePortal.getPluginLogger().atInfo().log(
                        "Exception placing portal block at %d, %d, %d: %s",
                        pos.x, pos.y, pos.z, e.getMessage()
                    );
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Log top-level exceptions
            HytalePortal.getPluginLogger().atInfo().log(
                "Exception in placePortalBlocks: {}",
                e.getMessage()
            );
            e.printStackTrace();
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
     * Remove blocks for a portal (when it's destroyed or replaced)
     */
    public void removePortalBlocks(Portal portal) {
        if (portal == null) {
            return;
        }

        // Remove from tracking set
        placedPortals.remove(portal.getCreationTime());

        try {
            World world = Universe.get().getWorld(portal.getWorldName());
            if (world == null) {
                return;
            }

            // Execute on world thread
            world.execute(() -> {
                try {
                    Vector3i[] framePositions = portal.getFramePositions();
                    for (Vector3i pos : framePositions) {
                        try {
                            long chunkCoord = calculateChunkCoordinate(pos);
                            var chunk = world.getChunkIfLoaded(chunkCoord);

                            if (chunk != null) {
                                // Use breakBlock to remove the block
                                chunk.breakBlock(pos.x, pos.y, pos.z);
                            }
                        } catch (Exception e) {
                            // Silently fail on individual block removal
                        }
                    }
                } catch (Exception e) {
                    HytalePortal.getPluginLogger().atInfo().log(
                        "Error removing portal blocks: {}",
                        e.getMessage()
                    );
                }
            });
        } catch (Exception e) {
            HytalePortal.getPluginLogger().atInfo().log(
                "Error in removePortalBlocks: {}",
                e.getMessage()
            );
        }
    }

    /**
     * Check if the visualizer is running
     */
    public boolean isRunning() {
        return particleTask != null && !particleTask.isCancelled();
    }
}
