package dev.jsemolik.hytaleportal.portal;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Represents a player's pair of portals (blue and orange).
 * Each player can have at most one blue portal and one orange portal active at a time.
 */
public class PortalPair {
    private final UUID playerUUID;
    private Portal bluePortal;
    private Portal orangePortal;

    public PortalPair(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.bluePortal = null;
        this.orangePortal = null;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Nullable
    public Portal getBluePortal() {
        return bluePortal;
    }

    @Nullable
    public Portal getOrangePortal() {
        return orangePortal;
    }

    /**
     * Set a portal for this player. Replaces any existing portal of the same type.
     * @return The old portal that was replaced, or null if there was no previous portal
     */
    @Nullable
    public Portal setPortal(Portal portal) {
        Portal oldPortal = null;
        if (portal.getType() == PortalType.BLUE) {
            oldPortal = this.bluePortal;
            this.bluePortal = portal;
        } else {
            oldPortal = this.orangePortal;
            this.orangePortal = portal;
        }
        return oldPortal;
    }

    /**
     * Get the opposite portal (if player walks through blue, return orange)
     */
    @Nullable
    public Portal getOppositePortal(PortalType type) {
        if (type == PortalType.BLUE) {
            return orangePortal;
        } else {
            return bluePortal;
        }
    }

    /**
     * Check if both portals are active
     */
    public boolean hasBothPortals() {
        return bluePortal != null && orangePortal != null;
    }

    /**
     * Remove a specific portal
     */
    public void removePortal(PortalType type) {
        if (type == PortalType.BLUE) {
            bluePortal = null;
        } else {
            orangePortal = null;
        }
    }

    /**
     * Remove all portals for this player
     */
    public void clearAllPortals() {
        bluePortal = null;
        orangePortal = null;
    }

    /**
     * Check if this portal pair has any active portals
     */
    public boolean hasAnyPortal() {
        return bluePortal != null || orangePortal != null;
    }

    @Override
    public String toString() {
        return "PortalPair{" +
                "player=" + playerUUID +
                ", hasBlue=" + (bluePortal != null) +
                ", hasOrange=" + (orangePortal != null) +
                '}';
    }
}
