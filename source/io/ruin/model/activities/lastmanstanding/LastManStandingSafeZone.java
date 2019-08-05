package io.ruin.model.activities.lastmanstanding;

import io.ruin.api.utils.Random;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum LastManStandingSafeZone {
    MOUNTAIN(3432, 5845, "mountain"),
    TRINITY_OUTPOST(3497, 5870, "Trinity outpost"),
    DEBTOR_HIDEOUT(3406, 5801, "Debtor hideout"),
    MOSER_SETTLEMENT(3474, 5787, "Moser settlement");

    private final int x, y;
    private final String text;

    private static final List<LastManStandingSafeZone> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

    LastManStandingSafeZone(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
    }

    public static LastManStandingSafeZone random()  {
        return VALUES.get(Random.get(VALUES.size() - 1));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getText() {
        return text;
    }
}
