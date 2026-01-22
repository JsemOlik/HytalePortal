package dev.jsemolik.hytaleportal.portal;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton manager for all portal pairs in the game.
 * Handles creation, removal, and lookup of portals for all players.
 */
public class PortalManager {
    private static PortalManager instance;
    
    // Map of player UUID to their portal pair
    private final Map<UUID, PortalPair> portalPairs;

    private PortalManager() {
        this.portalPairs = new ConcurrentHashMap<>();
    }

    /**
     * Get the singleton instance of PortalManager
     */
    public static PortalManager getInstance() {
        if (instance == null) {
            instance = new PortalManager();
        }
        return instance;
    }

    /**
     * Reset the singleton instance (useful for testing or plugin reload)
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Get or create a portal pair for a player
     */
    public PortalPair getOrCreatePortalPair(UUID playerUUID) {
        return portalPairs.computeIfAbsent(playerUUID, PortalPair::new);
    }

    /**
     * Get a player's portal pair (returns null if they don't have one)
     */
    @Nullable
    public PortalPair getPortalPair(UUID playerUUID) {
        return portalPairs.get(playerUUID);
    }

    /**
     * Create or replace a portal for a player
     */
    public void setPortal(UUID playerUUID, Portal portal) {
        dev.jsemolik.hytaleportal.HytalePortal.getPluginLogger().atInfo().log(
            "PortalManager.setPortal: Storing {} portal for player {}",
            portal.getType(), playerUUID
        );

        PortalPair pair = getOrCreatePortalPair(playerUUID);
        Portal oldPortal = pair.setPortal(portal);

        // Remove blocks from the old portal if it existed
        if (oldPortal != null) {
            dev.jsemolik.hytaleportal.HytalePortal.getPluginLogger().atInfo().log(
                "PortalManager.setPortal: Removing old {} portal",
                oldPortal.getType()
            );
            dev.jsemolik.hytaleportal.HytalePortal plugin = dev.jsemolik.hytaleportal.HytalePortal.getInstance();
            if (plugin != null && plugin.getPortalVisualizer() != null) {
                plugin.getPortalVisualizer().removePortalBlocks(oldPortal);
            }
        }

        dev.jsemolik.hytaleportal.HytalePortal.getPluginLogger().atInfo().log(
            "PortalManager.setPortal: Portal stored successfully. Total portal pairs: {}",
            portalPairs.size()
        );
    }

    /**
     * Remove a specific portal for a player
     */
    public void removePortal(UUID playerUUID, PortalType type) {
        PortalPair pair = portalPairs.get(playerUUID);
        if (pair != null) {
            // Get the portal before removing it
            Portal portalToRemove = (type == PortalType.BLUE) ? pair.getBluePortal() : pair.getOrangePortal();

            // Remove blocks
            if (portalToRemove != null) {
                dev.jsemolik.hytaleportal.HytalePortal plugin = dev.jsemolik.hytaleportal.HytalePortal.getInstance();
                if (plugin != null && plugin.getPortalVisualizer() != null) {
                    plugin.getPortalVisualizer().removePortalBlocks(portalToRemove);
                }
            }

            pair.removePortal(type);

            // Clean up the pair if it has no portals left
            if (!pair.hasAnyPortal()) {
                portalPairs.remove(playerUUID);
            }
        }
    }

    /**
     * Remove all portals for a player (e.g., when they disconnect)
     */
    public void removeAllPortals(UUID playerUUID) {
        PortalPair pair = portalPairs.get(playerUUID);
        if (pair != null) {
            // Remove blocks for both portals
            dev.jsemolik.hytaleportal.HytalePortal plugin = dev.jsemolik.hytaleportal.HytalePortal.getInstance();
            if (plugin != null && plugin.getPortalVisualizer() != null) {
                if (pair.getBluePortal() != null) {
                    plugin.getPortalVisualizer().removePortalBlocks(pair.getBluePortal());
                }
                if (pair.getOrangePortal() != null) {
                    plugin.getPortalVisualizer().removePortalBlocks(pair.getOrangePortal());
                }
            }
        }
        portalPairs.remove(playerUUID);
    }

    /**
     * Get all active portal pairs
     */
    public Map<UUID, PortalPair> getAllPortalPairs() {
        return Map.copyOf(portalPairs);
    }

    /**
     * Check if a player has a specific portal type
     */
    public boolean hasPortal(UUID playerUUID, PortalType type) {
        PortalPair pair = portalPairs.get(playerUUID);
        if (pair == null) {
            return false;
        }
        
        if (type == PortalType.BLUE) {
            return pair.getBluePortal() != null;
        } else {
            return pair.getOrangePortal() != null;
        }
    }

    /**
     * Check if a player has both portals active
     */
    public boolean hasBothPortals(UUID playerUUID) {
        PortalPair pair = portalPairs.get(playerUUID);
        return pair != null && pair.hasBothPortals();
    }

    /**
     * Get the total number of players with active portals
     */
    public int getActivePlayerCount() {
        return portalPairs.size();
    }

    /**
     * Get the total number of active portals across all players
     */
    public int getTotalPortalCount() {
        int count = 0;
        for (PortalPair pair : portalPairs.values()) {
            if (pair.getBluePortal() != null) count++;
            if (pair.getOrangePortal() != null) count++;
        }
        return count;
    }

    /**
     * Clear all portals (useful for plugin shutdown or reload)
     */
    public void clearAll() {
        // Remove blocks for all portals before clearing
        dev.jsemolik.hytaleportal.HytalePortal plugin = dev.jsemolik.hytaleportal.HytalePortal.getInstance();
        if (plugin != null && plugin.getPortalVisualizer() != null) {
            for (PortalPair pair : portalPairs.values()) {
                if (pair.getBluePortal() != null) {
                    plugin.getPortalVisualizer().removePortalBlocks(pair.getBluePortal());
                }
                if (pair.getOrangePortal() != null) {
                    plugin.getPortalVisualizer().removePortalBlocks(pair.getOrangePortal());
                }
            }
        }
        portalPairs.clear();
    }
}
