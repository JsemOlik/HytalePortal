package dev.jsemolik.hytaleportal.portal;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.UUID;

/**
 * Represents a single portal (blue or orange) in the world.
 * Portals are 2 blocks wide by 3 blocks tall.
 */
public class Portal {
    private final UUID ownerUUID;
    private final PortalType type;
    private final Vector3d position;      // Bottom-left corner position
    private final Vector3f rotation;      // Portal orientation (normal vector)
    private final String worldName;       // World the portal exists in
    private final long creationTime;      // When the portal was created

    /**
     * Portal dimensions (in blocks)
     */
    public static final int WIDTH = 2;
    public static final int HEIGHT = 3;

    public Portal(UUID ownerUUID, PortalType type, Vector3d position, Vector3f rotation, String worldName) {
        this.ownerUUID = ownerUUID;
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.worldName = worldName;
        this.creationTime = System.currentTimeMillis();
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public PortalType getType() {
        return type;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public String getWorldName() {
        return worldName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Get the center position of the portal (for teleportation calculations)
     */
    public Vector3d getCenterPosition() {
        return new Vector3d(
            position.x + (WIDTH / 2.0),
            position.y + (HEIGHT / 2.0),
            position.z + (WIDTH / 2.0)
        );
    }

    /**
     * Get the normal vector (forward direction) of the portal
     * This points outward from the surface the portal was placed on
     */
    public Vector3d getNormalVector() {
        // Convert yaw rotation to a direction vector
        // Yaw is stored in rotation.y
        float yaw = rotation.y;
        double radians = Math.toRadians(yaw);
        
        // Calculate normal vector pointing outward from portal surface
        return new Vector3d(
            -Math.sin(radians),
            0,
            Math.cos(radians)
        );
    }

    /**
     * Check if a given position is within the portal's boundaries
     */
    public boolean containsPosition(Vector3d pos) {
        // Simple bounding box check
        // TODO: Improve to account for rotation
        double minX = position.x;
        double maxX = position.x + WIDTH;
        double minY = position.y;
        double maxY = position.y + HEIGHT;
        double minZ = position.z;
        double maxZ = position.z + WIDTH;

        return pos.x >= minX && pos.x <= maxX &&
               pos.y >= minY && pos.y <= maxY &&
               pos.z >= minZ && pos.z <= maxZ;
    }

    /**
     * Get all block positions that make up the portal frame
     * Uses rotation to determine orientation
     */
    public Vector3i[] getFramePositions() {
        Vector3i[] positions = new Vector3i[WIDTH * HEIGHT];
        int index = 0;

        // Determine portal orientation based on rotation
        // rotation.y represents the yaw (horizontal direction the portal faces)
        float yaw = rotation.y;
        
        // Normalize yaw to 0-360
        while (yaw < 0) yaw += 360;
        while (yaw >= 360) yaw -= 360;
        
        // Determine if portal is on X-axis wall or Z-axis wall
        boolean isXAxis = (yaw >= 45 && yaw < 135) || (yaw >= 225 && yaw < 315);
        
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (isXAxis) {
                    // Portal spans along X axis (north/south facing wall)
                    positions[index++] = new Vector3i(
                        (int) position.x + x,
                        (int) position.y + y,
                        (int) position.z
                    );
                } else {
                    // Portal spans along Z axis (east/west facing wall)
                    positions[index++] = new Vector3i(
                        (int) position.x,
                        (int) position.y + y,
                        (int) position.z + x
                    );
                }
            }
        }

        return positions;
    }

    @Override
    public String toString() {
        return "Portal{" +
                "type=" + type +
                ", position=" + position +
                ", world=" + worldName +
                ", owner=" + ownerUUID +
                '}';
    }
}
