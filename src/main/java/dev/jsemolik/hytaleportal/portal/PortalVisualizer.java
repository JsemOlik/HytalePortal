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
     * Create particle effects for a portal
     * TODO: Once we have access to particle API, implement proper particle effects
     */
    private void createPortalParticles(Portal portal, World world) {
        // Get all frame positions
        Vector3i[] framePositions = portal.getFramePositions();
        
        // For now, we'll just log that we would create particles
        // Once the particle API is available, we can spawn actual particles
        
        // Particle colors based on portal type
        // Blue portal: RGB(0, 181, 226) - cyan/blue
        // Orange portal: RGB(255, 120, 0) - orange
        
        // TODO: Spawn particles at each frame position
        // TODO: Spawn swirling particles in the center of the portal
        // TODO: Add glow effect
        
        // Placeholder for future particle spawning
        /*
        for (Vector3i pos : framePositions) {
            // Spawn particle at this position
            // world.spawnParticle(particleType, pos.x(), pos.y(), pos.z(), ...);
        }
        
        // Spawn center particles
        Vector3d center = portal.getCenterPosition();
        // world.spawnParticle(particleType, center.x(), center.y(), center.z(), ...);
        */
    }
    
    /**
     * Check if the visualizer is running
     */
    public boolean isRunning() {
        return particleTask != null && !particleTask.isCancelled();
    }
}
