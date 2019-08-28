package io.ruin.model.activities.bosses.galvek;

import io.ruin.api.utils.Random;

public enum TsunamiDirection {
    NORTHERN(-13, 10),
    SOURTHERN(-13, -10);

    private static final TsunamiDirection[] DIRECTIONS = values();

    public final int offsetX;
    public final int offsetY;

    TsunamiDirection(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public static TsunamiDirection random() {
        return Random.get(DIRECTIONS);
    }
}
