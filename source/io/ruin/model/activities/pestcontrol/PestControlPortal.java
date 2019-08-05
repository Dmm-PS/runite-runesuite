package io.ruin.model.activities.pestcontrol;

import io.ruin.model.map.Position;

/**
 * An enumerated type whose elements correspond to each of the in-game portal types.
 *
 * @author Andys1814
 */
public enum PestControlPortal {
    WEST("western", "purple", 1743, 1739, new Position(2628, 2591, 0), new Position(2631, 2592, 0), 27, 23),
    EAST("eastern", "blue", 1744, 1740, new Position(2680, 2588, 0), new Position(2679, 2589, 0), 29, 24),
    SOUTHEAST("southeastern", "yellow", 1745, 1741, new Position(2669, 2570, 0), new Position(2670, 2573, 0), 31, 25),
    SOUTHWEST("southwestern", "red", 1746, 1742, new Position(2645, 2569, 0), new Position(2646, 2572, 0), 33, 26);

    private final String name;
    private final String color;
    private final int shieldedNpcId;
    private final int unshieldedNpcId;
    private final Position position;
    private final Position pestSpawnPosition;
    private final int shieldIconChildId;
    private final int healthChildId;
    private PestControlPortalStatus status;

    PestControlPortal(String name, String color, int shieldedNpcId, int unshieldedNpcId, Position position, Position pestSpawnPosition, int shieldIconChildId, int healthChildId) {
        this.name = name;
        this.color = color;
        this.shieldedNpcId = shieldedNpcId;
        this.unshieldedNpcId = unshieldedNpcId;
        this.position = position;
        this.pestSpawnPosition = pestSpawnPosition;
        this.shieldIconChildId = shieldIconChildId;
        this.healthChildId = healthChildId;
    }

    public static final PestControlPortal[] VALUES = values();


    public PestControlPortalStatus getStatus() {
        return status;
    }

    public void setStatus(PestControlPortalStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getShieldedNpcId() {
        return shieldedNpcId;
    }

    public int getUnshieldedNpcId( ){ return unshieldedNpcId; }

    public Position getPosition() {
        return position;
    }

    public Position getPestSpawnPosition() {
        return pestSpawnPosition;
    }

    public int getShieldIconChildId() {
        return shieldIconChildId;
    }

    public int getHealthChildId() {
        return healthChildId;
    }

}
