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
    }
    
    /**
     * Update particles for all active portals
     */
    private void updateParticles() {
        try {
            PortalManager manager = PortalManager.getInstance();
            var portalPairs = manager.getAllPortalPairs();

            // Debug log every 100 updates (every 5 seconds at 50ms interval)
            if (System.currentTimeMillis() % 5000 < 50) {
                HytalePortal.getPluginLogger().atInfo().log(
                    "PortalVisualizer.updateParticles: Processing {} portal pairs",
                    portalPairs.size()
                );
            }

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
     * Create visual effects for a portal using blocks
     */
    private void createPortalParticles(Portal portal, World world) {
        // Get all frame positions
        Vector3i[] framePositions = portal.getFramePositions();

        // Block type to use for visualization
        // Blue portal: Blue crystal blocks
        // Orange portal: Red crystal blocks
        String blockType = (portal.getType() == PortalType.BLUE) ?
            "Rock_Crystal_Blue_Small" :  // Blue portal - blue crystal
            "Rock_Crystal_Red_Small";     // Orange portal - red crystal

        try {
            // Log portal creation attempt
            HytalePortal.getPluginLogger().atInfo().log(
                "Creating {} portal with {} blocks at {} positions",
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
                            "Chunk not loaded for portal block at {}, {}, {}",
                            pos.x, pos.y, pos.z
                        );
                        continue;
                    }

                    // Use the BlockAccessor interface to place blocks
                    // Use Debug_Block for testing - it can be placed anywhere
                    // Settings: 0x100 = skip some validation
                    boolean placed = chunk.setBlock(pos.x, pos.y, pos.z, "Debug_Block");

                    if (!placed) {
                        // Log if placement failed
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Failed to place {} portal block at {}, {}, {} (setBlock returned false)",
                            portal.getType(), pos.x, pos.y, pos.z
                        );
                    } else {
                        HytalePortal.getPluginLogger().atInfo().log(
                            "Successfully placed {} portal block at {}, {}, {}",
                            portal.getType(), pos.x, pos.y, pos.z
                        );
                    }
                } catch (Exception e) {
                    // Log any exceptions for debugging
                    HytalePortal.getPluginLogger().atInfo().log(
                        "Exception placing portal block at {}, {}, {}: {}",
                        pos.x, pos.y, pos.z, e.getMessage()
                    );
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Log top-level exceptions
            HytalePortal.getPluginLogger().atInfo().log(
                "Exception in createPortalParticles: {}",
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
