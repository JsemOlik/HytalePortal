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
            position.x() + (WIDTH / 2.0),
            position.y() + (HEIGHT / 2.0),
            position.z() + (WIDTH / 2.0)
        );
    }

    /**
     * Check if a given position is within the portal's boundaries
     */
    public boolean containsPosition(Vector3d pos) {
        // Simple bounding box check
        // TODO: Improve to account for rotation
        double minX = position.x();
        double maxX = position.x() + WIDTH;
        double minY = position.y();
        double maxY = position.y() + HEIGHT;
        double minZ = position.z();
        double maxZ = position.z() + WIDTH;

        return pos.x() >= minX && pos.x() <= maxX &&
               pos.y() >= minY && pos.y() <= maxY &&
               pos.z() >= minZ && pos.z() <= maxZ;
    }

    /**
     * Get all block positions that make up the portal frame
     */
    public Vector3i[] getFramePositions() {
        Vector3i[] positions = new Vector3i[WIDTH * HEIGHT];
        int index = 0;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                positions[index++] = new Vector3i(
                    (int) position.x() + x,
                    (int) position.y() + y,
                    (int) position.z()
                );
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
